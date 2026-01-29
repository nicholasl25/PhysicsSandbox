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

        
        if (this.temperature > 2000) {
            this.texturepath = '/textures/Sun.jpg';
            this.type = PlanetTypes.STAR;
            
            // Three colors: red, white, blue 
            // TODO: Wien's Law - Temperature to color helper
            if (this.temperature > 10000) {
                this.color = new THREE.Color(0, 0, 1);
            }
            else if (this.temperature > 5000) {
                this.color = new THREE.Color(1, 1, 1);
            }
            else {
                this.color = new THREE.Color(1, 0, 0);
            }


        }

        else if (this.density > 2000) {
            this.texturepath = '/textures/Moon.jpg';
            this.type = PlanetTypes.ROCKY;
            this.color = new THREE.Color(0, 0, 0);
        }

        else {
            this.texturepath = '/textures/Juipter.jpg';
            this.type = PlanetTypes.GAS;
            this.color = new THREE.Color(0, 0, 0);
        }

    }

    getTexturepath() {return this.texturepath; }
    getColor() {return this.color; }
    getMass() {return this.mass; }
    getRadius() {return this.radius;}
    getTemperature() {return this.temperature; }

}
