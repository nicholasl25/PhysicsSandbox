/**
 * Optimizer landscape demo — analytic 2D losses and gradients.
 * Depends: (none)
 * Exposes: window.OptimizerLandscapes
 */
(function () {
    'use strict';

    /** One default η for every surface and optimizer so comparisons start from the same control. */
    const DEFAULT_LR = 0.05;

    window.OptimizerLandscapes = {
        bowl: {
            name: 'bowl',
            bounds: { xmin: -3, xmax: 3, ymin: -3, ymax: 3 },
            loss(x, y) { return  0.5*(x * x + y * y); },
            grad(x, y) { return [x, y]; },
            defaultStart: [-2.2, 2.4],
            defaultLr: DEFAULT_LR,
        },
        valley: {
            name: 'valley',
            bounds: { xmin: -2.5, xmax: 2.5, ymin: -2.5, ymax: 2.5 },
            loss(x, y) { return 0.5 * (8 * x * x + 0.35 * y * y); },
            grad(x, y) { return [8 * x, 0.35 * y]; },
            defaultStart: [-2, 2],
            defaultLr: DEFAULT_LR,
        },
        rosen: {
            name: 'rosen',
            bounds: { xmin: -0.8, xmax: 1.6, ymin: -0.3, ymax: 1.4 },
            loss(x, y) {
                const a = 1;
                const b = 100;
                const t1 = a - x;
                const t2 = y - x * x;
                return t1 * t1 + b * t2 * t2;
            },
            grad(x, y) {
                const b = 100;
                const gx = -2 * (1 - x) - 4 * b * x * (y - x * x);
                const gy = 2 * b * (y - x * x);
                return [gx, gy];
            },
            defaultStart: [-0.65, 1.15],
            defaultLr: DEFAULT_LR,
        },
    };
}());
