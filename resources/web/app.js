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

function canLaunchDesktopSims() {
    const h = window.location.hostname;
    return h === 'localhost' || h === '127.0.0.1';
}

async function launchSimulation(type) {
    try {
        // Gravity3D runs as a webapp, others launch Java
        if (type === 'gravity3d') {
            // Resolve against current URL so GitHub Pages project sites (/repo/) work (not root-absolute /gravity3d/…)
            window.location.href = new URL('gravity3d/gravity3d.html', window.location.href).toString();
            return;
        }
        if (!canLaunchDesktopSims()) {
            window.alert(
                'Desktop simulations need the local Java server. Clone this repository, run ./run.sh from its root, then open http://localhost:8080 in your browser.'
            );
            return;
        }
        const response = await fetch(`${window.location.origin}/api/launch/${type}`, {
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

