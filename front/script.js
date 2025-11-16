const form = document.getElementById("dataForm");
const result = document.getElementById("resultTable");

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const formData = new FormData(form);

  const r = parseFloat(formData.get("R"));
  const x = parseFloat(formData.get("X"));
  const y = parseFloat(formData.get("Y"));
  console.log("R: " + r + " X: " + x + " Y: " + y);

  errorBox.style.display = "none";
  errorBox.textContent = "";
  const errors = [];

  if (isNaN(r) || r < 2 || r > 5) {
    errors.push("R должен быть числом между 2 и 5.");
  }

  if (isNaN(y) || y < -3 || y > 5) {
    errors.push("Y должен быть числом между -3 и 5.");
  }

  if (errors.length > 0) {
    showError(errors.join("\n"));
    return;
  } else {
    try {
      const result = await sendRequest(formData);
      console.log("Server response:", result);
      
      addResultRow(result);

    } catch (error) {
      console.log("Error: " + error.message);
    }
  }
});

async function sendRequest(formData) {
  try {
      const params = new URLSearchParams(formData).toString();

      const response = await fetch("/fcgi-bin/server.jar", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params
      });

    if (!response.ok) throw new Error("Response is not ok: " + response);
    
    return await response.json();
  } catch (err) {
    throw err;
  }
}

function showError(message) {
  errorBox.textContent = message;
  errorBox.style.display = "block";
  errorBox.style.backgroundColor = "#d9534f";
}

function addResultRow(data) {
  const tableBody = document.getElementById("resultTable").getElementsByTagName("tbody")[0];
  
  const newRow = tableBody.insertRow(0);
  
  newRow.insertCell().textContent = data.x;
  newRow.insertCell().textContent = data.y;
  newRow.insertCell().textContent = data.r;
  newRow.insertCell().textContent = data.hit ? "Да" : "Нет";
  newRow.insertCell().textContent = data.currentTime;
  newRow.insertCell().textContent = data.executionTime;
}