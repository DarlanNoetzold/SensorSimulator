import React from "react";

const FileUploader = ({ onFileSelect }) => {
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      onFileSelect(file);
    }
  };

  return (
    <div>
      <label>Select File:</label>
      <input type="file" onChange={handleFileChange} accept=".csv" required />
    </div>
  );
};

export default FileUploader;
