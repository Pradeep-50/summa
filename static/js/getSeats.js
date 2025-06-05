// Fetch data from the Spring MVC backend
const getData = async () => {
  try {
    let url = "/api/buses"; // Adjust the endpoint according to your Spring MVC controller
    const res = await fetch(url);
    if (!res.ok) throw new Error('Network response was not ok');
    const data = await res.json();
    return data;
  } catch (err) {
    console.error("Error fetching bus data:", err);
  }
};

export default getData;

// Fetch upper deck seats from the Spring MVC backend
const upperDeck = async () => {
  try {
    let res = await fetch("/api/seats/upper"); // Adjust the endpoint according to your Spring MVC controller
    if (!res.ok) throw new Error('Network response was not ok');
    let data = await res.json();
    return data;
  } catch (err) {
    console.error("Error fetching upper deck seats:", err);
  }
};

// Fetch lower deck seats from the Spring MVC backend
const lowerDeck = async () => {
  try {
    let res = await fetch("/api/seats/lower"); // Adjust the endpoint according to your Spring MVC controller
    if (!res.ok) throw new Error('Network response was not ok');
    let data = await res.json();
    return data;
  } catch (err) {
    console.error("Error fetching lower deck seats:", err);
  }
};

export { upperDeck, lowerDeck };