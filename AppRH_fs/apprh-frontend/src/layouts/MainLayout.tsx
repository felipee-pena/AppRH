import { Outlet } from "react-router-dom";

import { Header } from "../components/header/Header";
import { Footer } from "../components/footer/Footer";

import "./main-layout.css";

export function MainLayout() {
  return (
    <div className="main-layout">
      <Header />

      <main className="main-layout__content">
        <Outlet />
      </main>

      <Footer />
    </div>
  );
}