const PlanetTypes = {
        ROCKY: "Rocky", GAS: "Gas", STAR: "Star", BLACKHOLE: "Black Hole", 
    };

class State {

    texturepath = null;
    type = null;
    color = null;

    constructor(mass, radius, temperature) {
        this.mass = mass;
        this.radius = radius;
        this.temperature = temperature;

        this.volume = Math.PI * 4 * Math.pow(this.radius, 3) / 3;
        this.density = this.mass / this.volume;

        
        if (this.temperature >= 2000) {
            this.texturepath = '/textures/Sun.jpg';
            this.type = PlanetTypes.STAR;
            this.color = this.temperatureToColor();
        }

        else if (this.density >= 2000) {
            this.texturepath = '/textures/Moon.jpg';
            this.type = PlanetTypes.ROCKY;
            this.color = new THREE.Color(1, 1, 1);
        }

        else {
            this.texturepath = '/textures/Jupiter.jpg';
            this.type = PlanetTypes.GAS;
            this.color = new THREE.Color(1, 1, 1);
        }

    }

    temperatureToColor() {
        // Approximation of Wien's Law that converts temperature to RGB values
        const T = this.temperature / 100;

        let R, G, B;

        // Red
        if (T <= 75) {
            R = 255;
        } else {
            R = 330 * Math.pow(T - 60, -0.13);
        }

        // Green
        if (T <= 75) {
            G = 100 * Math.log(T) - 175;
        } else {
            G = 280 * Math.pow(T - 60, -0.075);
        }

        // Blue
        if (T >= 95) {
            B = 255;
        } else if (T <= 30) {
            B = 0;
        } else {
            B = 130 * Math.log(T - 25) - 305;
        }

        R = Math.min(255, Math.max(0, Math.round(R))) / 255;
        G = Math.min(255, Math.max(0, Math.round(G))) / 255;
        B = Math.min(255, Math.max(0, Math.round(B))) / 255;

          // --- Simple saturation boost (change as needed) ---
        const boost = 3;
        const avg = (R + G + B) / 3;

        R = avg + (R - avg) * boost;
        G = avg + (G - avg) * boost;
        B = avg + (B - avg) * boost;

        

        return new THREE.Color(R, G, B);
     }

    getTexturepath() {return this.texturepath; }
    getColor() {return this.color; }
    getMass() {return this.mass; }
    getRadius() {return this.radius;}
    getTemperature() {return this.temperature; }
    getType() {return this.type; }

}
