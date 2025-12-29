async function launchSimulation(type) {
    try {
        const response = await fetch(`/api/launch/${type}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            console.log(`Launched ${type} simulation`);
        } else {
            const error = await response.text();
            alert(`Error launching simulation: ${error}`);
        }
    } catch (error) {
        alert(`Error: ${error.message}`);
    }
}

