// ============================================================================
// 1. YOUR SETTINGS (FILL THESE IN)
// ============================================================================

const APP_SECRET = "Bearer YOUR_SUPER_SECRET_AUTH_TOKEN_HERE";

const FIREBASE_PROJECT_ID = "your-firebase-project-id";
const FIREBASE_CLIENT_EMAIL = "firebase-adminsdk-xxxxx@your-firebase-project-id.iam.gserviceaccount.com";
const FIREBASE_PRIVATE_KEY = `-----BEGIN PRIVATE KEY-----
Paste Your Private Key Here
Exactly As It Appears In The JSON File
Including The Begin And End Tags
-----END PRIVATE KEY-----`;

// Paste your two device tokens here after you install the apps and do the 7-Tap Trick
const TOKENS = {
  "her": "PASTE_HER_FCM_TOKEN_HERE",
  "him": "PASTE_HIS_FCM_TOKEN_HERE"
};

// ============================================================================
// 2. CORE LOGIC (DO NOT TOUCH BELOW HERE)
// ============================================================================

export default {
  async fetch(request, env, ctx) {
    const authHeader = request.headers.get("Authorization");
    if (authHeader !== APP_SECRET) {
      return new Response("Unauthorized", { status: 401 });
    }
    if (request.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    try {
      const body = await request.json();
      const actionType = body.action; 
      const targetDevice = body.target; // "her" or "him"

      const targetFcmToken = TOKENS[targetDevice];
      if (!targetFcmToken) {
        return new Response("Unknown target device", { status: 400 });
      }

      const accessToken = await getGoogleAccessToken();

      // Send DIRECTLY to specific device token with HIGH priority (Bypasses Doze mode instantly)
      const fcmPayload = {
        message: {
          token: targetFcmToken,
          data: {
            action: actionType
          },
          android: {
            priority: "high"
          }
        }
      };

      const fcmResponse = await fetch(`https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${accessToken}`
        },
        body: JSON.stringify(fcmPayload)
      });

      if (fcmResponse.ok) {
        return new Response("Success", { status: 200 });
      } else {
        const err = await fcmResponse.text();
        return new Response("FCM Error: " + err, { status: 500 });
      }
    } catch (e) {
      return new Response("Bad Request: " + e.message, { status: 400 });
    }
  }
};

// ============================================================================
// 3. CRYPTOGRAPHY ENGINE (JWT & OAuth)
// ============================================================================
async function getGoogleAccessToken() {
  const header = { alg: "RS256", typ: "JWT" };
  const now = Math.floor(Date.now() / 1000);
  const claim = {
    iss: FIREBASE_CLIENT_EMAIL,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now
  };

  const encodedHeader = base64url(JSON.stringify(header));
  const encodedClaim = base64url(JSON.stringify(claim));
  const signatureInput = `${encodedHeader}.${encodedClaim}`;

  const signature = await signRSA(signatureInput, FIREBASE_PRIVATE_KEY);
  const jwt = `${signatureInput}.${signature}`;

  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`
  });

  const data = await response.json();
  return data.access_token;
}

async function signRSA(data, pemKey) {
  const pemHeader = "-----BEGIN PRIVATE KEY-----";
  const pemFooter = "-----END PRIVATE KEY-----";
  const pemContents = pemKey.substring(pemKey.indexOf(pemHeader) + pemHeader.length, pemKey.indexOf(pemFooter)).replace(/\s/g, '');
  
  const binaryDer = Uint8Array.from(atob(pemContents), c => c.charCodeAt(0));
  
  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryDer.buffer,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const encoder = new TextEncoder();
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", cryptoKey, encoder.encode(data));
  
  return base64url(signature);
}

function base64url(source) {
  let encoded = typeof source === 'string' ? btoa(source) : btoa(String.fromCharCode.apply(null, new Uint8Array(source)));
  return encoded.replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}
