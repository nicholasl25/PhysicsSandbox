/**
 * Optimizer landscape demo — stepping (GD, SGD noise, SGD+momentum, Adagrad, RMSProp, Adam).
 * Depends: app.getParams(kind) for per-method hyperparameters.
 * Exposes: window.OptimizerCore
 */
(function () {
    'use strict';

    function randn() {
        let u = 0;
        let v = 0;
        while (u === 0) u = Math.random();
        while (v === 0) v = Math.random();
        return Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
    }

    function resetOptimizerState() {
        return {
            m: [0, 0],
            v: [0, 0],
            vel: [0, 0],
            gacc: [0, 0],
            rms: [0, 0],
            t: 0,
        };
    }

    /**
     * @param {string} kind
     * @param {{ batch?: number }} params
     */
    function gradForStep(kind, params, gx, gy) {
        if (kind !== 'sgd' && kind !== 'sgdm') return [gx, gy];
        const B = Math.max(1, parseInt(String(params.batch), 10) || 1);
        const scale = 0.25 / Math.sqrt(B);
        return [gx + scale * randn(), gy + scale * randn()];
    }

    /**
     * @param {object} runner - { kind, theta, optState, trail, steps, convergedStep }
     * @param {object} app - { getLand, getParams }
     */
    function stepRunner(runner, app) {
        const L = app.getLand();
        const p = app.getParams(runner.kind);
        const lr = p.lr;
        const eps = p.eps;
        const o = runner.kind;
        const [gx0, gy0] = L.grad(runner.theta[0], runner.theta[1]);
        const [gx, gy] = gradForStep(o, p, gx0, gy0);
        const theta = runner.theta;
        const st = runner.optState;

        if (o === 'gd' || o === 'sgd') {
            theta[0] -= lr * gx;
            theta[1] -= lr * gy;
        } else if (o === 'sgdm') {
            const mu = p.momentum;
            const m = Number.isFinite(mu) ? Math.min(Math.max(mu, 0), 0.999) : 0.9;
            st.vel[0] = m * st.vel[0] + gx;
            st.vel[1] = m * st.vel[1] + gy;
            theta[0] -= lr * st.vel[0];
            theta[1] -= lr * st.vel[1];
        } else if (o === 'adagrad') {
            st.gacc[0] += gx * gx;
            st.gacc[1] += gy * gy;
            theta[0] -= lr * gx / (Math.sqrt(st.gacc[0]) + eps);
            theta[1] -= lr * gy / (Math.sqrt(st.gacc[1]) + eps);
        } else if (o === 'rmsprop') {
            const rho = p.rho;
            const r = Number.isFinite(rho) ? Math.min(Math.max(rho, 0), 0.999) : 0.9;
            st.rms[0] = r * st.rms[0] + (1 - r) * gx * gx;
            st.rms[1] = r * st.rms[1] + (1 - r) * gy * gy;
            theta[0] -= lr * gx / (Math.sqrt(st.rms[0]) + eps);
            theta[1] -= lr * gy / (Math.sqrt(st.rms[1]) + eps);
        } else if (o === 'adam') {
            const b1 = p.b1;
            const b2 = p.b2;
            st.t += 1;
            const t = st.t;
            st.m[0] = b1 * st.m[0] + (1 - b1) * gx;
            st.m[1] = b1 * st.m[1] + (1 - b1) * gy;
            st.v[0] = b2 * st.v[0] + (1 - b2) * gx * gx;
            st.v[1] = b2 * st.v[1] + (1 - b2) * gy * gy;
            const mhx = st.m[0] / (1 - Math.pow(b1, t));
            const mhy = st.m[1] / (1 - Math.pow(b1, t));
            const vhx = st.v[0] / (1 - Math.pow(b2, t));
            const vhy = st.v[1] / (1 - Math.pow(b2, t));
            theta[0] -= lr * mhx / (Math.sqrt(vhx) + eps);
            theta[1] -= lr * mhy / (Math.sqrt(vhy) + eps);
        }

        runner.steps += 1;
        runner.trail.push([theta[0], theta[1]]);
        const [gxt, gyt] = L.grad(theta[0], theta[1]);
        return Math.hypot(gxt, gyt);
    }

    window.OptimizerCore = {
        randn,
        resetOptimizerState,
        gradForStep,
        stepRunner,
    };
}());
