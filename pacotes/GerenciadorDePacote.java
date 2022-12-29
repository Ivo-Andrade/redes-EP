package pacotes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

public class GerenciadorDePacote 
{

    private static int TAMANHO_INT = 4;
    private static int TAMANHO_LONG = 8;

    private static int TAMANHO_ID = TAMANHO_INT;
    private static int TAMANHO_NUM_PACOTE = TAMANHO_INT;
    private static int TAMANHO_CHECKSUM = TAMANHO_LONG;

    private static int TAMANHO_CABECALHO = 
        TAMANHO_CHECKSUM + TAMANHO_ID + TAMANHO_ID + TAMANHO_NUM_PACOTE;

    public static int getTamanhoCabecalho ()
    {
        return TAMANHO_CABECALHO;
    }

    public static byte[] construirPacote ( 
        int idRemetente, 
        int idDestinatario, 
        int numDoPacoteNaSequencia, 
        byte[] bytesDeDados
    )
    {

        byte[] bytesDoIdDoRemetente = 
            ByteBuffer.allocate( TAMANHO_ID )
                .putInt( idRemetente )
                .array();

        byte[] bytesDoIdDoDestinatario = 
            ByteBuffer.allocate( TAMANHO_ID )
                .putInt( idDestinatario )
                .array();
        
        byte[] bytesDoNumDoPacoteNaSequencia = 
            ByteBuffer.allocate( TAMANHO_NUM_PACOTE )
                .putInt( numDoPacoteNaSequencia )
                .array();
        
        CRC32 checksumDoPacote = new CRC32();
        checksumDoPacote.update( bytesDoIdDoRemetente );
        checksumDoPacote.update( bytesDoIdDoDestinatario );
        checksumDoPacote.update( bytesDoNumDoPacoteNaSequencia );
        checksumDoPacote.update( bytesDeDados );
        
        byte[] bytesDaChecksum = 
            ByteBuffer.allocate( TAMANHO_CHECKSUM )
                .putLong( checksumDoPacote.getValue() )
                .array();
        
        ByteBuffer bufferDoPacote = ByteBuffer.allocate(
            TAMANHO_CABECALHO + bytesDeDados.length 
        );
        bufferDoPacote.put( bytesDaChecksum );
        bufferDoPacote.put( bytesDoIdDoRemetente );
        bufferDoPacote.put( bytesDoIdDoDestinatario );
        bufferDoPacote.put( bytesDoNumDoPacoteNaSequencia );
        bufferDoPacote.put( bytesDeDados );

        return bufferDoPacote.array();

    }

    public static boolean verificarPacote ( byte[] pacote, int length )
    {

        byte[] bytesDaChecksumRecebida = 
            Arrays.copyOfRange( pacote, 0, TAMANHO_CHECKSUM );

        byte[] bytesDoPayload = 
            Arrays.copyOfRange( pacote, TAMANHO_CHECKSUM, length );
        
        CRC32 checksumDoPacote = new CRC32();
        checksumDoPacote.update( bytesDoPayload );
        
        byte[] bytesDaChecksumCalculada = 
            ByteBuffer.allocate( TAMANHO_CHECKSUM )
                .putLong( checksumDoPacote.getValue() )
                .array();

        return Arrays.equals( bytesDaChecksumRecebida, bytesDaChecksumCalculada );

    }

    public static int decodificarIdDoRemetente ( byte[] pacote ) 
    {
        byte[] bytesDoIdDoRemetente = 
            Arrays.copyOfRange( 
                pacote, 
                TAMANHO_CHECKSUM, 
                TAMANHO_CHECKSUM + TAMANHO_ID 
            );

        return ByteBuffer.wrap( bytesDoIdDoRemetente ).getInt();
    }

    public static int decodificarIdDoDestinatario ( byte[] pacote ) 
    {
        byte[] bytesDoIdDoDestinatario = 
            Arrays.copyOfRange( 
                pacote, 
                TAMANHO_CHECKSUM + TAMANHO_ID, 
                TAMANHO_CHECKSUM + TAMANHO_ID + TAMANHO_ID 
            );

        return ByteBuffer.wrap( bytesDoIdDoDestinatario ).getInt();      
    }

    public static int decodificarNumDoPacote ( byte[] pacote ) 
    {
        byte[] bytesDoNumDoPacote = 
            Arrays.copyOfRange( 
                pacote, 
                TAMANHO_CHECKSUM + TAMANHO_ID + TAMANHO_ID, 
                TAMANHO_CHECKSUM + TAMANHO_ID + TAMANHO_ID + TAMANHO_NUM_PACOTE 
            );

        return ByteBuffer.wrap( bytesDoNumDoPacote ).getInt();
    }

    public static byte[] decodificarDados ( byte[] pacote ) 
    {
        return
            Arrays.copyOfRange( 
                pacote, 
                TAMANHO_CABECALHO, 
                pacote.length
            );
    }
 
}
