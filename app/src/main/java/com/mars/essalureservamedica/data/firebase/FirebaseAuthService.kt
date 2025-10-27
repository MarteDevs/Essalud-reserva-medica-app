package com.mars.essalureservamedica.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Un servicio contenedor para Firebase Authentication que simplifica las operaciones de autenticación.
 * Proporciona métodos suspendidos para el inicio de sesión y el registro, y maneja las conversiones de Task a Result de corutinas.
 */
class FirebaseAuthService {
    // Obtiene la instancia compartida del objeto FirebaseAuth.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Autentica a un usuario con su correo electrónico y contraseña.
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un objeto [Result] que contiene el [FirebaseUser] en caso de éxito o una excepción en caso de fallo.
     */
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> = try {
        // Llama al metodo de inicio de sesión de Firebase y espera a que se complete de forma asíncrona.
        val result = auth.signInWithEmailAndPassword(email, password).await()
        // Si tiene éxito, envuelve el objeto de usuario no nulo en un Result de éxito.
        Result.success(result.user!!)
    } catch (e: Exception) {
        // Si se produce una excepción, la envuelve en un Result de fallo.
        Result.failure(e)
    }

    /**
     * Registra a un nuevo usuario con una dirección de correo electrónico y una contraseña.
     * @param email El correo electrónico del usuario a registrar.
     * @param password La contraseña del usuario a registrar.
     * @return Un objeto [Result] que contiene el [FirebaseUser] recién creado en caso de éxito o una excepción en caso de fallo.
     */
    suspend fun signUpWithEmailPassword(email: String, password: String): Result<FirebaseUser> = try {
        // Llama al metodo de creación de usuario de Firebase y espera a que se complete.
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        // Si tiene éxito, envuelve el objeto de usuario no nulo en un Result de éxito.
        Result.success(result.user!!)
    } catch (e: Exception) {
        // Si se produce una excepción, la envuelve en un Result de fallo.
        Result.failure(e)
    }

    /**
     * Autentica a un usuario utilizando una cuenta de Google Sign-In.
     * @param account El objeto [GoogleSignInAccount] obtenido del flujo de Google Sign-In.
     * @return Un objeto [Result] que contiene el [FirebaseUser] en caso de éxito o una excepción en caso de fallo.
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> = try {
        // Crea una credencial de Firebase a partir del token de ID de la cuenta de Google.
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        // Inicia sesión en Firebase con la credencial y espera el resultado.
        val result = auth.signInWithCredential(credential).await()
        // Si tiene éxito, envuelve el objeto de usuario no nulo en un Result de éxito.
        Result.success(result.user!!)
    } catch (e: Exception) {
        // Si se produce una excepción, la envuelve en un Result de fallo.
        Result.failure(e)
    }

    /**
     * Cierra la sesión del usuario actualmente autenticado.
     */
    fun signOut() {
        // Llama al metodo de cierre de sesión de Firebase.
        auth.signOut()
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     * @return El objeto [FirebaseUser] actual, o nulo si no hay ningún usuario autenticado.
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
