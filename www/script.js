document.addEventListener('DOMContentLoaded', function() {
    console.log('JavaScript loaded successfully!');
    
    const button = document.getElementById('testBtn');
    const output = document.getElementById('output');
    
    button.addEventListener('click', function() {
        const timestamp = new Date().toLocaleTimeString();
        output.innerHTML = `
            <h3>Success! ✓</h3>
            <p>JavaScript is working correctly.</p>
            <p>Button clicked at: ${timestamp}</p>
            <p>This demonstrates the server correctly serves .js files with application/javascript MIME type.</p>
        `;
    });
    
    // Initial message
    output.innerHTML = '<p>Page loaded. Click the button to test JavaScript execution.</p>';
});
