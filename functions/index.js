const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.deleteUserFromAuth = functions.region('us-central1').https.onCall(async (data, context) => {
  // Verificación detallada de autenticación
  console.log('Headers recibidos:', context.rawRequest.headers);

  if (!context.auth) {
    console.error('Fallo de autenticación - contexto auth vacío');
    throw new functions.https.HttpsError('unauthenticated', 'Debes iniciar sesión para realizar esta acción', {
      detail: 'El token de autenticación no fue proporcionado o es inválido'
    });
  }

  const { uid } = data;
  const callerUid = context.auth.uid;

  console.log(`Solicitud de eliminación - Caller: ${callerUid}, Target: ${uid}`);

  // Verificación de auto-eliminación
  if (uid === callerUid) {
    throw new functions.https.HttpsError('failed-precondition', 'No puedes eliminarte a ti mismo');
  }

  // Resto de la implementación...
  try {
    // Verificar que el usuario existe en Auth
    await admin.auth().getUser(uid);

    // Eliminar usuario
    await admin.auth().deleteUser(uid);

    return { success: true, message: 'Usuario eliminado correctamente' };
  } catch (error) {
    console.error('Error en deleteUserFromAuth:', error);

    if (error.code === 'auth/user-not-found') {
      return { success: true, message: 'Usuario no existía en Authentication' };
    }

    throw new functions.https.HttpsError('internal', error.message);
  }
});