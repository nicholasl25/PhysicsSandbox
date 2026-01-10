function toggleDropdown(id) {
    const content = document.getElementById(id + '-content');
    const options = document.getElementById(id + '-options');
    const arrow = document.getElementById(id + '-arrow');
    
    const isExpanded = content.classList.contains('expanded');
    
    if (isExpanded) {
        content.classList.remove('expanded');
        options.classList.remove('expanded');
        arrow.textContent = '▼';
    } else {
        content.classList.add('expanded');
        options.classList.add('expanded');
        arrow.textContent = '▲';
    }
}

async function launchSimulation(type) {
    try {
        // For 3D simulation, redirect to web-based version
        if (type === 'gravity3d') {
            window.location.href = '/gravity3d.html';
            return;
        }
        
        // For 2D and Schwarzschild, use the old Java window approach
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

