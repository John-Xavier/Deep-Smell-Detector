if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/service-worker.js')
        .then((registration) => {
            console.log('Service Worker registered with scope:', registration.scope);
        })
        .catch((error) => {
            console.log('Service Worker registration failed:', error);
        });
}

function clearFormAndResults() {
    //document.querySelector('[name="javaCode"]').value = '';
    hideErrorMessage();
    document.getElementById('results').innerHTML = '';
}


function clearTextArea() {
    document.getElementById('codeTextarea').value = '';
}


function hideErrorMessage() {
    document.getElementById('error-message').style.display = 'none';
    $(".error-message-result").hide();
}
$(document).ready(function () {
    $("form").submit(function (event) {
        event.preventDefault();
        const textareaValue = document.querySelector('[name="javaCode"]').value.trim();
        if (!textareaValue) {
            event.preventDefault();
            document.getElementById('error-message').style.display = 'block';
            return;
        }
        $("#loading").show();
        var formData = $(this).serialize(); // Gets the data from the form

        $.post("http://localhost:8080/detect", formData, function (response) {
            $("#loading").hide();
            if (response.errorStatus === "Java Class is invalid, Please try again") {
                var errorMessage = "This Java class is invalid, please use a different class.";
                $("#results").html(`<div class="failed-message">${errorMessage}</div>`);
                return; // Exit the function early
            }

            var resultsHtml = `<div class="results-container"><h4>Results:</h4>`;
            var hasSmell = false;

            // Initialize the table with headings
            resultsHtml += `
        <table class="table">
            <thead>
                <tr>
                    <th>Detected Code Smell</th>
                    <th>Class/Method Name</th>
                    <th>Suggested Fixes</th>
                </tr>
            </thead>
            <tbody>
    `;

            // Check each code smell and add corresponding rows
            if (response.lm_output === 1) {
                resultsHtml += `
            <tr>
                <td>Long Method Smell</td>
                <td>${response.long_method_name}</td>
                <td>Consider breaking down the method into smaller, more focused methods or turn the method into its own class.</td>
            </tr>
        `;
                hasSmell = true;
            }

            if (response.gc_output === 1) {
                resultsHtml += `
            <tr>
                <td>God Class Smell</td>
                <td>${response.className}</td>
                <td>If the class is handling multiple responsibilities, identify cohesive groups of class attributes and methods that can be extracted into their own class.</td>
            </tr>
        `;
                hasSmell = true;
            }

            if (response.fe_output === 1) {
                resultsHtml += `
            <tr>
                <td>Feature Envy Smell</td>
                <td>${response.featureEnvy_method_name}</td>
                <td>Move method to another class or consider creating a new class.</td>
            </tr>
        `;
                hasSmell = true;
            }


            resultsHtml += `</tbody></table>`;
            resultsHtml += `</div>`;

            // If no smells are detected, display a message
            if (!hasSmell) {
                resultsHtml = `<div class="success-message"><h4>No code smells detected.</h4></div>`;
            }

            $("#results").html(resultsHtml); // Updates the results div with the results or the message
        }, "json").fail(function (jqXHR, textStatus, errorThrown) {
            $("#loading").hide();
            // Handle the error here
            var errorMessage = "There was a network issue. Please check your connection and try again.";
            if (jqXHR.status === 0) {
                errorMessage = "Not connected. Please verify your network connection.";
            } else if (jqXHR.status == 404) {
                errorMessage = "Requested page not found [404].";
            } else if (jqXHR.status == 500) {
                errorMessage = "Internal Server Error [500].";
            } else if (textStatus === "parsererror") {
                errorMessage = "Requested JSON parse failed.";
            } else if (textStatus === "timeout") {
                errorMessage = "Time out error.";
            } else if (textStatus === "abort") {
                errorMessage = "Ajax request aborted.";
            }
            $("#results").html(`<div class="failed-message">${errorMessage}</div>`);
        });
    });
});
