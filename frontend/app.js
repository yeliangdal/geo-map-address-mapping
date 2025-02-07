let map;

document.addEventListener("DOMContentLoaded", () => {
    map = L.map("map").setView([37.7749, -122.4194], 5); // Default view (San Francisco)
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
    }).addTo(map);
});

async function fetchCoordinates() {
    const postcodeList = document.getElementById("postcodeList").value
        .split("\n")
        .map(pc => pc.trim())
        .filter(Boolean);

    if (postcodeList.length === 0) {
        alert("Please enter at least one postcode.");
        return;
    }

    try {
        const requestUrl = "http://localhost:8080/api/geocode?postcodes=" + postcodeList.join(",")
        console.log(requestUrl)
        const response = await fetch(requestUrl, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const locations = await response.json();
        updateMap(locations);
    } catch (error) {
        console.error("Error fetching data:", error);
        alert("Failed to fetch location data. Check backend connection.");
    }
}

function updateMap(locations) {
    locations.forEach(location => {
        const { postcode, lat, lon } = location;
        if (lat && lon) {
            L.marker([lat, lon])
                .addTo(map)
                .bindPopup(`<b>Postcode: ${postcode}</b>`)
                .openPopup();
        }
    });
}
