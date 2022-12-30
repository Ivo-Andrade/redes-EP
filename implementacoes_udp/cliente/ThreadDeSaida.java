package implementacoes_udp.cliente;

// import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
// import java.io.File;
// import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;

import pacotes.GerenciadorDePacote;

public class ThreadDeSaida 
    extends Thread 
{

    private final UDPdoCliente udp;
                
    private boolean acabouCriacaoDePacotes;

    public ThreadDeSaida (
        UDPdoCliente udp
    )
        throws Exception
    {
        this.udp = udp;
        this.acabouCriacaoDePacotes = false;
    }

    public void run ()
    {

        try {
            
            // BufferedWriter writer = 
            //     new BufferedWriter( 
            //         new FileWriter(
            //             new File (
            //                 "input",
            //                 "input-" 
            //                 , udp.getDenominacao() 
            //                 , ".txt"
            //             )
            //         ) 
            //     );
            // writer.write( udp.getMensagemDeEnvio() );
            // writer.close();

            String mensagemCriptografada =
                udp.getCriptografia()
                    .codificarMensagem(
                        udp.getMensagemDeEnvio()
                    );

            InputStream streamDaMensagem =
                new ByteArrayInputStream(
                    mensagemCriptografada.getBytes()
                );

            while ( ! this.acabouCriacaoDePacotes )
            {

                if (
                    udp.getProxNumDaSequenciaDePacotes()
                    < udp.getBaseDeEnvio() + udp.getTamanhoDeJanelaDePacotes()
                )
                {

                    udp.getSemaforoDasVarsDeJanela().acquire();

                    byte[] pacoteParaEnvio = preparePacote( streamDaMensagem );

                    udp.enviePacote( pacoteParaEnvio );

                    System.out.println( 
                        udp.getDenominacao() 
                        + ": Envio do pacote " 
                        + udp.getProxNumDaSequenciaDePacotes() 
                    );

                    udp.getSemaforoDasVarsDeTimeout().acquire();

                    udp.adicioneTimeout( 
                        udp.getProxNumDaSequenciaDePacotes(),
                        pacoteParaEnvio
                    );

                    udp.getSemaforoDasVarsDeTimeout().release();

                    udp.incrementeProxNumDaSequenciaDePacotes();

                    udp.getSemaforoDasVarsDeJanela().release();

                }

                sleep( 1 );

            }

        } 
        catch ( Exception e ) 
        {
            e.printStackTrace();
            System.exit( -1 );
        }

    }

    private byte[] preparePacote( InputStream streamDaMensagem )
        throws Exception
    {

        byte[] bufferDeMsg = 
            new byte[ 
                ( udp.getTamanhoDoPacote() - GerenciadorDePacote.getTamanhoCabecalho() ) 
            ];
        int tamanhoDoPayload = 
            streamDaMensagem.read(
                bufferDeMsg, 
                0, 
                ( udp.getTamanhoDoPacote() - GerenciadorDePacote.getTamanhoCabecalho() )
            );

        if ( tamanhoDoPayload == -1 ) 
        {

            this.acabouCriacaoDePacotes = true;

            return GerenciadorDePacote
                .construirPacote(
                    udp.getIdDeCliente(),
                    udp.getIdDoServidor(),
                    udp.getProxNumDaSequenciaDePacotes(), 
                    new byte[0]
                );

        }
        else
        {

            byte[] bytesDoPayload = 
                Arrays.copyOfRange( bufferDeMsg, 0, tamanhoDoPayload );

            return GerenciadorDePacote
                    .construirPacote( 
                        udp.getIdDeCliente(),
                        udp.getIdDoServidor(),
                        udp.getProxNumDaSequenciaDePacotes(), 
                        bytesDoPayload
                    );

        }

    }

}
