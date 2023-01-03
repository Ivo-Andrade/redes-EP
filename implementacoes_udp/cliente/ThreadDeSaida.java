package implementacoes_udp.cliente;

// import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
// import java.io.File;
// import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map.Entry;

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

            while ( ! udp.aTransferenciaTerminou() )
            {

                udp.getSemaforoDasVars().acquire();

                if (
                    (
                        ! udp.existemPacotesEmTimeout()
                        && ! udp.verificarACKdaJanelaDeCongestionamentoAnterior()
                    )
                ) {

                    if ( udp.getProxNumDaSequenciaDePacotes() != 0 ) 
                    {
                        udp.getSemaforoDasVars().release();
                        continue;
                    }
                    
                }

                if (
                    ! udp.existemPacotesEmTimeout()
                ) {

                    if ( udp.getTamanhoDeJanelaDePacotes() > udp.getJanelaDeCongestionamento() ) 
                    {
                        udp.incrementeJanelaDeCongestionamento();
                    }

                    udp.configureBaseDaJanelaDeCongestionamento( 
                        udp.getProxNumDaSequenciaDePacotes(),
                        udp.getJanelaDeCongestionamento()
                    );
                }
    
                System.out.println( 
                    udp.getDenominacao() 
                    + ": ------------------------------------------ INICIO DE JANELA "
                    + udp.getJanelaDeCongestionamento()
                );

                for ( 
                    int i = udp.getBaseDaJanelaDeCongestionamento(); 
                    i < udp.getBaseDaJanelaDeCongestionamento() + udp.getJanelaDeCongestionamento(); 
                    i++
                ) {

                    if ( udp.existemPacotesEmTimeout() )
                    {

                        udp.reduzaJanelaDeCongestionamento();
    
                        Entry<Integer, byte[]> pacote = udp.obterPacoteEmTimeout();
    
                        udp.enviePacote( pacote.getValue() );
    
                        System.out.println( 
                            udp.getDenominacao()
                            + ": Re-envio do pacote em timeout " 
                            + pacote.getKey()
                        );
    
                        udp.adicioneTimeout( 
                            pacote.getKey(),
                            pacote.getValue()
                        );
    
                    }
                    else if (
                        ! this.acabouCriacaoDePacotes
                        && udp.getProxNumDaSequenciaDePacotes()
                        < udp.getBaseDeEnvio() + udp.getTamanhoDeJanelaDePacotes()
                    )
                    {
    
                        byte[] pacoteParaEnvio = preparePacote( streamDaMensagem );
    
                        udp.enviePacote( pacoteParaEnvio );
    
                        System.out.println( 
                            udp.getDenominacao() 
                            + ": Envio do pacote " 
                            + udp.getProxNumDaSequenciaDePacotes() 
                        );
    
                        udp.adicioneTimeout( 
                            udp.getProxNumDaSequenciaDePacotes(),
                            pacoteParaEnvio
                        );
    
                        udp.incrementeProxNumDaSequenciaDePacotes();
    
                    }
                    
                }
    
                System.out.println( 
                    udp.getDenominacao() 
                    + ": ------------------------------------------ FIM DE JANELA"
                );

                udp.getSemaforoDasVars().release();

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