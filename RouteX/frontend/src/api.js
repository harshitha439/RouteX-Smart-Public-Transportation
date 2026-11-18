const API = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function api(path, { method = "GET", body } = {}) {
  const token = localStorage.getItem("token");
  const res = await fetch(`${API}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const details = data.details ? " " + Object.values(data.details).join(", ") : "";
    throw new Error((data.error || "Request failed") + details);
  }
  return data;
}
