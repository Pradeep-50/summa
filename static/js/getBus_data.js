const getData = async () => {
  try {
    // Update the URL to point to your Spring MVC backend endpoint
    let url = "/api/buses"; // Adjust this endpoint according to your Spring MVC controller
    const res = await fetch(url);
    
    // Check if the response is OK (status code 200-299)
    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }
    
    const data = await res.json();
    return data;
  } catch (err) {
    console.error("Error fetching bus data:", err);
  }
};

export default getData;
