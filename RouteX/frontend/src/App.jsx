import { useState, useEffect } from "react";
import { Routes, Route, Navigate, Link, useNavigate } from "react-router-dom";
import { api } from "./api";

const HOME = { PASSENGER: "/passenger", DRIVER: "/driver", ADMIN: "/admin", AUTHORITY: "/admin" };

function AuthForm({ mode }) {
  const nav = useNavigate();
  const [f, setF] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [err, setErr] = useState("");
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setErr("");
    try {
      const r = await api(`/api/auth/${mode}`, { method: "POST", body: f });
      localStorage.setItem("token", r.token);
      localStorage.setItem("role", r.role);
      nav(HOME[r.role]); // single login, redirect by role
    } catch (ex) { setErr(ex.message); }
  };

  return (
    <form className="card" onSubmit={submit}>
      <h2>{mode === "login" ? "Login" : "Register as Passenger"}</h2>
      {mode === "register" && <>
        <input placeholder="Full name" value={f.fullName} onChange={set("fullName")} required />
        <input placeholder="Phone" value={f.phone} onChange={set("phone")} />
      </>}
      <input type="email" placeholder="Email" value={f.email} onChange={set("email")} required />
      <input type="password" placeholder="Password (min 8)" value={f.password} onChange={set("password")} required />
      {err && <p className="error">{err}</p>}
      <button>{mode === "login" ? "Login" : "Create account"}</button>
    </form>
  );
}

function Dashboard({ title, allow }) {
  const nav = useNavigate();
  const [me, setMe] = useState(null);
  const [err, setErr] = useState("");
  const [msg, setMsg] = useState("");
  const [pw, setPw] = useState({ currentPassword: "", newPassword: "" });

  const logout = () => {
    api("/api/auth/logout", { method: "POST" }).catch(() => {});
    localStorage.clear();
    nav("/login");
  };

  useEffect(() => { api("/api/users/me").then(setMe).catch(() => logout()); }, []);

  if (!me) return <p className="card">Loading...</p>;
  if (me.role !== allow && !(allow === "ADMIN" && me.role === "AUTHORITY")) return <Navigate to={HOME[me.role]} />;

  const save = async (e) => {
    e.preventDefault(); setErr(""); setMsg("");
    try { setMe(await api("/api/users/me", { method: "PUT", body: me })); setMsg("Profile saved"); }
    catch (ex) { setErr(ex.message); }
  };
  const changePw = async (e) => {
    e.preventDefault(); setErr(""); setMsg("");
    try {
      await api("/api/users/me/password", { method: "PUT", body: pw });
      setMsg("Password changed");
      setPw({ currentPassword: "", newPassword: "" });
    } catch (ex) { setErr(ex.message); }
  };
  const field = (k) => ({ value: me[k] ?? "", onChange: (e) => setMe({ ...me, [k]: e.target.value }) });

  return (
    <div className="card">
      <h2>{title} Dashboard</h2>
      <p>Welcome, <b>{me.fullName}</b> ({me.role})</p>
      <button onClick={logout}>Logout</button>
      <form onSubmit={save}>
        <h3>Profile</h3>
        <input placeholder="Full name" {...field("fullName")} />
        <input placeholder="Phone" {...field("phone")} />
        <input placeholder="Emergency contact name" {...field("emergencyContactName")} />
        <input placeholder="Emergency contact phone" {...field("emergencyContactPhone")} />
        <button>Save profile</button>
      </form>
      <form onSubmit={changePw}>
        <h3>Change password</h3>
        <input type="password" placeholder="Current password" value={pw.currentPassword}
               onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} />
        <input type="password" placeholder="New password (min 8)" value={pw.newPassword}
               onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} />
        <button>Change password</button>
      </form>
      {msg && <p className="ok">{msg}</p>}{err && <p className="error">{err}</p>}
    </div>
  );
}

export default function App() {
  return (
    <>
      <nav className="nav"><b>🚌 RouteX</b><Link to="/">Home</Link><Link to="/about">About</Link>
        <Link to="/login">Login</Link><Link to="/register">Register</Link></nav>
      <Routes>
        <Route path="/" element={<div className="card"><h1>RouteX</h1><p>Smart Public Transportation Management System</p></div>} />
        <Route path="/about" element={<div className="card"><p>Built iteratively using Adaptive Software Engineering.</p></div>} />
        <Route path="/login" element={<AuthForm mode="login" />} />
        <Route path="/register" element={<AuthForm mode="register" />} />
        <Route path="/passenger" element={<Dashboard title="Passenger" allow="PASSENGER" />} />
        <Route path="/driver" element={<Dashboard title="Driver" allow="DRIVER" />} />
        <Route path="/admin" element={<Dashboard title="Admin" allow="ADMIN" />} />
      </Routes>
    </>
  );
}
