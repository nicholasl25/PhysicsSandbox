function toggleDropdown(id) {
    const content = document.getElementById(id + '-content');
    const options = document.getElementById(id + '-options');
    const arrow = document.getElementById(id + '-arrow');
    
    if (!content || !options || !arrow) return;
    
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
        // Gravity3D runs as a webapp, others launch Java
        if (type === 'gravity3d') {
            // Open the Gravity3D webapp in a new window/tab
            window.location.href = '/gravity3d/gravity3d.html';
        } else {
            // Launch Java applications for Gravity2D and Schwarzchild
            const response = await fetch(`/api/launch/${type}`, {
                method: 'POST'
            });
            
            if (response.ok) {
                console.log(`Launched ${type} simulation`);
            } else {
                const error = await response.text();
                alert(`Error launching simulation: ${error}`);
            }
        }
    } catch (error) {
        alert(`Error: ${error.message}`);
    }
}

