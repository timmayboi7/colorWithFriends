const palette = [
  { name: 'Sunset', hex: '#ff6b6b' },
  { name: 'Coral', hex: '#ff9f7a' },
  { name: 'Gold', hex: '#fbbf24' },
  { name: 'Mint', hex: '#34d399' },
  { name: 'Sea', hex: '#22d3ee' },
  { name: 'Sky', hex: '#60a5fa' },
  { name: 'Indigo', hex: '#4f46e5' },
  { name: 'Lilac', hex: '#a78bfa' },
  { name: 'Pink', hex: '#ec4899' },
  { name: 'Sand', hex: '#f5e0b7' },
  { name: 'Olive', hex: '#84cc16' },
  { name: 'Charcoal', hex: '#1f2937' },
];

const paletteContainer = document.getElementById('palette');
const previewColor = document.getElementById('preview-color');
const colorValue = document.getElementById('color-value');
const copyButton = document.getElementById('copy-button');
const shareHint = document.getElementById('share-hint');
const nameInput = document.getElementById('name-input');
const friendBadge = document.getElementById('friend-badge');

function setBadge(name) {
  friendBadge.textContent = name ? `${name} picked this` : '';
  friendBadge.style.display = name ? 'inline-block' : 'none';
}

function updatePreview(hex, source = 'palette') {
  previewColor.style.background = hex;
  colorValue.textContent = hex;
  const params = new URLSearchParams(window.location.search);
  params.set('color', hex);
  const name = nameInput.value.trim();
  if (name) {
    params.set('name', name);
  } else {
    params.delete('name');
  }
  const shareUrl = `${window.location.origin}${window.location.pathname}?${params.toString()}`;
  copyButton.dataset.shareUrl = shareUrl;
  shareHint.textContent = source === 'palette'
    ? 'Share a link to open this color.'
    : 'Color loaded from your friend.';
}

function createSwatch(color) {
  const button = document.createElement('button');
  button.className = 'swatch';
  button.type = 'button';
  button.setAttribute('aria-label', `${color.name} ${color.hex}`);

  const swatchColor = document.createElement('div');
  swatchColor.className = 'swatch__color';
  swatchColor.style.background = color.hex;

  const label = document.createElement('div');
  label.className = 'swatch__label';
  label.innerHTML = `<span>${color.name}</span><span>${color.hex}</span>`;

  button.appendChild(swatchColor);
  button.appendChild(label);
  button.addEventListener('click', () => updatePreview(color.hex));
  return button;
}

function hydrateFromQuery() {
  const params = new URLSearchParams(window.location.search);
  const color = params.get('color');
  const name = params.get('name');

  if (name) {
    nameInput.value = name;
    setBadge(name);
  }

  if (color && /^#([0-9a-fA-F]{3}){1,2}$/i.test(color)) {
    updatePreview(color, 'link');
  } else {
    updatePreview(palette[0].hex, 'palette');
  }
}

function copyShareLink() {
  const shareUrl = copyButton.dataset.shareUrl;
  if (!shareUrl) return;

  navigator.clipboard.writeText(shareUrl).then(() => {
    copyButton.textContent = 'Copied!';
    setTimeout(() => { copyButton.textContent = 'Copy share link'; }, 1200);
  }).catch(() => {
    shareHint.textContent = 'Clipboard blocked. Manually copy the URL.';
  });
}

function setup() {
  palette.forEach((color) => {
    const swatch = createSwatch(color);
    paletteContainer.appendChild(swatch);
  });

  copyButton.addEventListener('click', copyShareLink);
  nameInput.addEventListener('input', () => setBadge(nameInput.value.trim()));

  hydrateFromQuery();
}

setup();
