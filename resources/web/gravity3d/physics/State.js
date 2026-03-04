const PlanetTypes = {
        ROCKY: "Rocky", GAS: "Gas", STAR: "Star", BLACKHOLE: "Black Hole", 
    };

class State {

    texturepath = null;
    type = null;
    color = null;

    /** @param {number} mass kg, @param {number} radius m, @param {number} temperature K */
    constructor(mass, radius, temperature, consts) {
        this.mass = mass;
        this.radius = radius;
        this.temperature = temperature;
        this.consts = consts;

        this.volume = Math.PI * 4 * Math.pow(this.radius, 3) / 3;
        this.density = this.mass / this.volume;

        if (this.getSchwarzschildRadius() >= this.radius) {
            this.texturepath = '/textures/Blackhole.jpg';
            this.type = PlanetTypes.BLACKHOLE;
            this.radius = this.getSchwarzschildRadius();
        }
        
        else if (this.temperature >= 2000) {
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

        this.luminosity = this.getLuminosity();

    }

    /** Returns RGB color from temperature; uses this.temperature (K). */
    temperatureToColor() {
    
        const T = this.temperature; // Temperature in Kelvin
        let R, G, B;
    
        if (T >= 30000) {
            // O-type: blue (155, 176, 255)
            R = 155; G = 176; B = 255;
        } else if (T >= 10000) {
            // B-type: blue-white (185, 210, 255)
            R = 185; G = 210; B = 255;
        } else if (T >= 7500) {
            // A-type: white (255, 255, 255)
            R = 255; G = 255; B = 255;
        } else if (T >= 6000) {
            // F-type: yellow-white (255, 255, 230)
            R = 255; G = 255; B = 230;
        } else if (T >= 5200) {
            // G-type: yellow (like Sun) (255, 235, 180)
            R = 255; G = 235; B = 180;
        } else if (T >= 3700) {
            // K-type: orange (255, 180, 100)
            R = 255; G = 180; B = 100;
        } else {
            // M-type: red (255, 100, 50)
            R = 255; G = 100; B = 50;
        }
    
        return new THREE.Color(R/255, G/255, B/255);
    }

    /** Returns Schwarzschild radius in m. */
    getSchwarzschildRadius() {
        return 2 * this.consts.G * this.mass / (this.consts.c ** 2);
    }

    /** Returns luminosity in L☉ (solar units); 0 for non-stars. */
    getLuminosity() {
        if (this.type == PlanetTypes.STAR) {
            // Stefan - Boltzman Law for star luminosity
            const luminosity = 4 * Math.PI * (this.temperature ** 4) * (this.radius ** 2) * this.consts.σ;

            // Luminosity of sun in our universe (with standard boltzman constat)
            const liminositySun = 3.83e26;

            // Return ratio of Luminosity between our object and the sun
            return luminosity / liminositySun;
        }

        return 0;
        
    }

    /** Returns surface flux L/(4πR²) in solar flux units (ratio to Sun); 0 for non-stars. */
    getLuminosityPerArea() {
        if (this.type === PlanetTypes.STAR) {
            const T_SUN = 5778;
            return Math.pow(this.temperature / T_SUN, 4);
        }
        return 0;
    }




    /** Returns texture path string. */
    getTexturepath() {return this.texturepath; }
    /** Returns THREE.Color. */
    getColor() {return this.color; }
    /** Returns mass in kg. */
    getMass() {return this.mass; }
    /** Returns radius in m. */
    getRadius() {return this.radius;}
    /** Returns temperature in K. */
    getTemperature() {return this.temperature; }
    /** Returns type string (e.g. "Star", "Rocky"). */
    getType() {return this.type; }

}
