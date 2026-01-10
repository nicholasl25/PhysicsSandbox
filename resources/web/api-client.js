/**
 * API Client for communicating with the Java backend.
 * Handles REST API calls and state polling.
 */

class APIClient {
    constructor(baseURL = 'http://localhost:8080') {
        this.baseURL = baseURL;
        this.simulationId = null;
        this.pollingInterval = null;
        this.onStateUpdate = null;
    }

    /**
     * Creates a new 3D simulation and returns its ID.
     */
    async createSimulation3D() {
        try {
            const response = await fetch(`${this.baseURL}/api/simulations`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ dimension: '3' })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.simulationId = data.id;
            return this.simulationId;
        } catch (error) {
            console.error('Error creating simulation:', error);
            throw error;
        }
    }

    /**
     * Gets the current simulation state.
     */
    async getState() {
        if (!this.simulationId) {
            throw new Error('No simulation ID. Call createSimulation3D() first.');
        }

        try {
            const response = await fetch(`${this.baseURL}/api/simulations/${this.simulationId}/state`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error getting state:', error);
            throw error;
        }
    }

    /**
     * Adds a planet to the simulation.
     */
    async addPlanet(planetData) {
        if (!this.simulationId) {
            throw new Error('No simulation ID. Call createSimulation3D() first.');
        }

        try {
            const response = await fetch(`${this.baseURL}/api/simulations/${this.simulationId}/planets`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(planetData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || `HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error adding planet:', error);
            throw error;
        }
    }

    /**
     * Updates simulation settings.
     */
    async updateSettings(settings) {
        if (!this.simulationId) {
            throw new Error('No simulation ID. Call createSimulation3D() first.');
        }

        try {
            const response = await fetch(`${this.baseURL}/api/simulations/${this.simulationId}/settings`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(settings)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || `HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error updating settings:', error);
            throw error;
        }
    }

    /**
     * Starts polling for simulation state updates.
     * Calls onStateUpdate callback with each update.
     */
    startPolling(callback, intervalMs = 16) { // ~60 FPS
        if (this.pollingInterval) {
            this.stopPolling();
        }

        this.onStateUpdate = callback;
        
        const poll = async () => {
            try {
                const state = await this.getState();
                if (this.onStateUpdate) {
                    this.onStateUpdate(state);
                }
            } catch (error) {
                console.error('Polling error:', error);
            }
        };

        // Poll immediately
        poll();
        
        // Then poll at interval
        this.pollingInterval = setInterval(poll, intervalMs);
    }

    /**
     * Stops polling for updates.
     */
    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
        this.onStateUpdate = null;
    }
}
