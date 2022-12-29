package criptografia;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CriptografiaAES {
    
    private static String valorDaChave = "valorSuperSecretoAcordadoViaRSAanterior";

    private static SecretKeySpec chaveSecreta;
    private static byte[] chave;

    private final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private final String CHARSET = "UTF-8";
    private final String ALGORITMO_MSG_DIGEST = "SHA-1";
    private final String ALGORITMO_CHAVE_SECRETA = "AES";

    public CriptografiaAES() 
        throws Exception
    {

        MessageDigest sha = null;

        chave = valorDaChave.getBytes( CHARSET );
        sha = MessageDigest.getInstance( ALGORITMO_MSG_DIGEST );
        chave = sha.digest( chave );
        chave = Arrays.copyOf( chave, 16 );
        chaveSecreta = new SecretKeySpec( chave, ALGORITMO_CHAVE_SECRETA );

    }

    public String codificarMensagem ( String mensagem ) 
        throws Exception 
    {

        Cipher cipher = Cipher.getInstance( TRANSFORMATION );
        cipher.init( Cipher.ENCRYPT_MODE, chaveSecreta );
        return Base64.getEncoder()
          .encodeToString(
                cipher.doFinal( mensagem.getBytes( CHARSET ) )
            );

    }

    public String decodificarMensagem ( String mensagemCriptografada ) 
        throws Exception 
    {

        Cipher cipher = Cipher.getInstance( TRANSFORMATION );
        cipher.init( Cipher.DECRYPT_MODE, chaveSecreta );
        return new String(
            cipher.doFinal(
                Base64.getDecoder()
                    .decode( mensagemCriptografada )
                )
            );

    }
    
}
