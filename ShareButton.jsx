import React, { useState } from "react";

export default function ShareButton({ title, text, url }) {
  const [open, setOpen] = useState(false);
  const [copied, setCopied] = useState(false);

  const shareData = {
    title: title || document.title,
    text: text || "Check this out!",
    url: url || window.location.href,
  };

  // Native share (mobile)
  const handleNativeShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share(shareData);
      } catch (err) {
        console.log("Share cancelled", err);
      }
    } else {
      setOpen(true);
    }
  };

  // Copy link fallback
  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(shareData.url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      alert("Failed to copy link");
    }
  };

  // Social share links
  const socialLinks = {
    facebook: `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(
      shareData.url
    )}`,
    twitter: `https://twitter.com/intent/tweet?text=${encodeURIComponent(
      shareData.text
    )}&url=${encodeURIComponent(shareData.url)}`,
    whatsapp: `https://api.whatsapp.com/send?text=${encodeURIComponent(
      shareData.text + " " + shareData.url
    )}`,
    telegram: `https://t.me/share/url?url=${encodeURIComponent(
      shareData.url
    )}&text=${encodeURIComponent(shareData.text)}`,
    linkedin: `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(
      shareData.url
    )}`,
    email: `mailto:?subject=${encodeURIComponent(
      shareData.title
    )}&body=${encodeURIComponent(shareData.text + " " + shareData.url)}`,
  };

  return (
    <div>
      {/* Share Button */}
      <button style={styles.button} onClick={handleNativeShare}>
        🔗 Share
      </button>

      {/* Modal */}
      {open && (
        <div style={styles.modalOverlay} onClick={() => setOpen(false)}>
          <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: "0 0 10px 0", color: "#1e293b", fontWeight: "bold" }}>Share this</h3>

            <div style={styles.grid}>
              <a href={socialLinks.facebook} target="_blank" rel="noopener noreferrer" style={styles.link}>Facebook</a>
              <a href={socialLinks.twitter} target="_blank" rel="noopener noreferrer" style={styles.link}>X</a>
              <a href={socialLinks.whatsapp} target="_blank" rel="noopener noreferrer" style={styles.link}>WhatsApp</a>
              <a href={socialLinks.telegram} target="_blank" rel="noopener noreferrer" style={styles.link}>Telegram</a>
              <a href={socialLinks.linkedin} target="_blank" rel="noopener noreferrer" style={styles.link}>LinkedIn</a>
              <a href={socialLinks.email} style={styles.link}>Email</a>
            </div>

            <button style={styles.copyBtn} onClick={copyToClipboard}>
              {copied ? "Copied!" : "Copy Link"}
            </button>

            <button style={styles.closeBtn} onClick={() => setOpen(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  button: {
    padding: "10px 16px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    background: "#111",
    color: "#fff",
    fontSize: "14px",
    display: "inline-flex",
    alignItems: "center",
    gap: "6px",
    fontWeight: "bold",
  },
  modalOverlay: {
    position: "fixed",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: "rgba(0,0,0,0.5)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 1000,
  },
  modal: {
    background: "#fff",
    padding: "20px",
    borderRadius: "12px",
    width: "300px",
    textAlign: "center",
    boxShadow: "0 10px 25px rgba(0,0,0,0.2)",
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "10px",
    margin: "15px 0",
  },
  link: {
    padding: "8px",
    borderRadius: "6px",
    background: "#f1f5f9",
    color: "#0f172a",
    fontSize: "12px",
    textDecoration: "none",
    fontWeight: "600",
    display: "block",
  },
  copyBtn: {
    marginTop: "10px",
    padding: "8px",
    width: "100%",
    border: "none",
    borderRadius: "6px",
    background: "#2563eb",
    color: "#fff",
    cursor: "pointer",
    fontWeight: "bold",
  },
  closeBtn: {
    marginTop: "8px",
    padding: "8px",
    width: "100%",
    border: "none",
    borderRadius: "6px",
    background: "#ddd",
    cursor: "pointer",
    fontWeight: "bold",
    color: "#475569",
  },
};
