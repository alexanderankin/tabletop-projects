import { useState } from "react";

function Center() {
  const [editingName, setEditingName] = useState(false);
  const [name, setName] = useState("");
  /*
  return <div className="bg-overlay d-flex align-items-center">
    <div className="container text-center text-white">
      <h1 className="display-4">Centered Cropped Background</h1>
      <p className="lead">On smaller screens, left and right are cropped automatically.</p>
      <button className="btn btn-primary btn-lg">Example Button</button>
    </div>
  </div>;
  */
  return <>
    <main className="col-lg-9 ms-sm-auto col-xl-10 px-md-4 text-white">
      <div className="pt-4 pb-2 mb-3">
        {editingName ? <h3>Name your character:</h3> : <h3>Creating:</h3>}
        {name === "" || editingName
          ? <form onSubmit={e => {
            e.preventDefault();
            setName(new FormData(e.target as HTMLFormElement).get("name") as string);
            setEditingName(false);
          }}>
            <input defaultValue={name} type="text" name="name" className="form-control" />
          </form>
          : <h1 className="h2" onClick={() => setEditingName(true)}>
            {name}
          </h1>}
        {/*<p>This layout mimics Bootstrap documentation examples.</p>
        <button className="btn btn-primary">Action</button>*/}
      </div>
    </main>
  </>;
}

function Sidebar() {
  if (2 > 1)
    return null;

  return <>
    <nav id="sidebarMenu"
         className="col-lg-3 col-xl-2 d-lg-block bg-dark sidebar collapse">
      <div className="position-sticky pt-3 text-white">
        <ul className="nav nav-pills flex-column mb-auto">
          <li className="nav-item">
            <a href="#" className="nav-link active text-white">Dashboard</a>
          </li>
          <li>
            <a href="#" className="nav-link text-white">Orders</a>
          </li>
          <li>
            <a href="#" className="nav-link text-white">Products</a>
          </li>
          <li>
            <a href="#" className="nav-link text-white">Customers</a>
          </li>
        </ul>
      </div>
    </nav>
  </>;
}

function MainLayout() {
  return (
    <div className="bg-overlay d-flex flex-column vh-100 pt-5">
      <div className="container-fluid flex-grow-1 d-flex">
        <div className="row flex-fill w-100">
          <Sidebar />
          <Center />
        </div>
      </div>
    </div>
  );
}

function TopBar() {
  return <>
    <nav className="navbar navbar-dark bg-dark fixed-top">
      <div className="container-fluid">
        <button className="btn btn-outline-light d-lg-none" id="sidebarToggle">
          ☰
        </button>
        <a className="navbar-brand ms-2" href="#">
          DnD Character Generator
        </a>
        {/*<div className="d-flex">
          <a href="#" className="nav-link text-light">

          </a>
        </div>*/}
      </div>
    </nav>

  </>;
}


export function App() {
  return <>
    <TopBar />
    <MainLayout />
  </>;
}
