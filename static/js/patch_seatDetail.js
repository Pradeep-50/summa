const patchSeat_upper = async (updatedData, id) => {
  let url = `/api/seats/upper/${id}`; // Adjust the endpoint according to your Spring MVC controller
  let res = await fetch(url, {
    method: "PATCH",
    body: JSON.stringify(updatedData),
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (!res.ok) throw new Error('Network response was not ok');
};

const patchSeat_lower = async (updatedData, id) => {
  let url = `/api/seats/lower/${id}`; // Adjust the endpoint according to your Spring MVC controller
  let res = await fetch(url, {
    method: "PATCH",
    body: JSON.stringify(updatedData),
    headers: {
      "Content-Type": "application/json",
    },
  });
  if (!res.ok) throw new Error('Network response was not ok');
};

export { patchSeat_upper, patchSeat_lower };