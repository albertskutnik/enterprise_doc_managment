import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";

const API_URL = "http://localhost:8080";

function App() {
  const [file, setFile] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [link, setLink] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    getDocuments();
  }, []);

  async function getDocuments() {
    const response = await fetch(API_URL + "/api/documents");
    const data = await response.json();
    setDocuments(data);
  }

  async function uploadFile() {
    if (file === null) {
      setMessage("Najpierw wybierz plik");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(API_URL + "/api/documents", {
      method: "POST",
      body: formData
    });

    if (response.ok) {
      setMessage("Plik wyslany");
      await getDocuments();
    } else {
      setMessage("Blad wysylania pliku");
    }
  }

  async function createLink(id) {
    const response = await fetch(API_URL + "/api/documents/" + id + "/share-links", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        expiresInHours: 24
      })
    });

    const data = await response.json();
    setLink(data.downloadUrl);
  }

  return (
    <div>
      <h1>Dokumenty</h1>

      <input
        type="file"
        onChange={(event) => setFile(event.target.files[0])}
      />

      <button onClick={uploadFile}>
        Wyslij
      </button>

      <p>{message}</p>

      <h2>Lista plikow</h2>

      {documents.map((document) => (
        <div key={document.id}>
          <p>{document.name}</p>

          <button onClick={() => createLink(document.id)}>
            Utworz link
          </button>
        </div>
      ))}

      <h2>Link</h2>

      <input value={link} readOnly />
    </div>
  );
}

createRoot(document.getElementById("root")).render(<App />);