const API_URL = '/api/risk';

// Live Search Suggestions
document.getElementById('appNameInput').addEventListener('input', async (e) => {
    const query = e.target.value.trim();
    const datalist = document.getElementById('appSuggestions');

    // Don't search for short strings or URLs
    if (query.length < 1 || query.startsWith('http')) {
        return;
    }

    try {
        const response = await fetch(`/api/risk/search?query=${encodeURIComponent(query)}`);
        if (response.ok) {
            const suggestions = await response.json();
            datalist.innerHTML = ''; // Clear previous
            suggestions.forEach(name => {
                const option = document.createElement('option');
                option.value = name;
                datalist.appendChild(option);
            });
        }
    } catch (err) {
        console.error("Error fetching suggestions", err);
    }
});

function showSection(sectionId) {
    document.querySelectorAll('section').forEach(sec => {
        sec.classList.remove('active-section');
        sec.classList.add('hidden-section');
    });
    const active = document.getElementById(sectionId);
    active.classList.remove('hidden-section');
    active.classList.add('active-section');
}

async function checkApp() {
    const appNameInput = document.getElementById('appNameInput');
    const appName = appNameInput.value.trim();
    if (!appName) {
        alert("Please enter an application name.");
        return;
    }

    try {
        const response = await fetch(`/api/risk/check?appName=${encodeURIComponent(appName)}`);

        if (response.ok) {
            const result = await response.json();
            displayResult(result);
            showSection('result');
        } else if (response.status === 404) {
            document.getElementById('notFoundMsg').style.display = 'block';
            document.getElementById('manualFields').classList.remove('hidden-section');
            document.getElementById('appNameHidden').value = appName;
        } else {
            alert('Error checking app.');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// Handle Form Submission
document.getElementById('riskForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const data = {
        appName: document.getElementById('appNameHidden').value || document.getElementById('appNameInput').value,
        exposureLevel: formData.get('exposureLevel'),
        userConsent: formData.get('userConsent'),
        dataSensitivity: formData.get('dataSensitivity'),
        retentionPeriod: formData.get('retentionPeriod')
    };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorData = await response.json();
            alert('Error: ' + JSON.stringify(errorData));
            return;
        }

        const result = await response.json();
        displayResult(result);
        showSection('result');
    } catch (error) {
        console.error('Error submitting form:', error);
        alert('An unexpected error occurred. Please try again.');
    }
});

function displayResult(result) {
    const scoreValue = document.getElementById('scoreValue');
    const riskCategory = document.getElementById('riskCategory');
    const riskExplanation = document.getElementById('riskExplanation');
    const appNameResult = document.getElementById('appNameResult') || createResultHeader();

    // Set App Name in Result
    appNameResult.textContent = `Risk Score for: ${result.appName}`;

    // Animate Score
    animateValue(scoreValue, 0, result.finalRiskScore, 1000);

    riskCategory.textContent = result.riskCategory;
    riskCategory.className = ''; // Reset class

    if (result.riskCategory.includes('Low')) {
        riskCategory.classList.add('low-risk');
        riskExplanation.textContent = "Great job! Your system has strong privacy safeguards.";
    } else if (result.riskCategory.includes('Medium')) {
        riskCategory.classList.add('medium-risk');
        riskExplanation.textContent = "Your system has moderate risks. Consider improving consent or data retention policies.";
    } else {
        riskCategory.classList.add('high-risk');
        riskExplanation.textContent = "Warning! High privacy risk detected. Immediate action required.";
    }
}

function createResultHeader() {
    const container = document.querySelector('.result-container');
    const h2 = container.querySelector('h2');
    const p = document.createElement('h3');
    p.id = 'appNameResult';
    p.style.color = '#94a3b8';
    p.style.marginBottom = '1rem';
    container.insertBefore(p, h2.nextSibling);
    return p;
}

function animateValue(obj, start, end, duration) {
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        obj.innerHTML = Math.floor(progress * (end - start) + start);
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}
