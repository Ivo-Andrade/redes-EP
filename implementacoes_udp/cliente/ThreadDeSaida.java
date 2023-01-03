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

            udp.setInicioDeTransmissao();

            while ( ! udp.aTransferenciaTerminou() )
            {

                udp.getSemaforoDeCongestionamento().acquire();
                udp.getSemaforoDeReenvios().acquire();
                udp.getSemaforoDeFluxo().acquire();
                udp.getSemaforoDeTimeouts().acquire();                

                if ( udp.existemPacotesEmTimeout() )
                {

                    udp.reduzaJanelaDeCongestionamento();

                    System.out.println( "RECUPERACAO RAPIDA - " + udp.getJanelaDeCongestionamento() );

                    Entry<Integer, byte[]> pacoteParaEnvio = udp.removerPacoteEmTimeout();

                    udp.enviePacote( pacoteParaEnvio.getValue() );

                    System.out.println( 
                        udp.getDenominacao() 
                        + ": Re-Envio do pacote em timeout " 
                        + pacoteParaEnvio.getKey() 
                    );

                    udp.adicionarTimeout( pacoteParaEnvio.getKey() , pacoteParaEnvio.getValue() );

                    udp.configureBaseDaJanelaDeCongestionamento( pacoteParaEnvio.getKey(), 1 );

                    System.out.println( "RECUPERACAO RAPIDA - FIM " + pacoteParaEnvio.getKey() + " " + 1 );

                }
                else if ( udp.verificarACKdaJanelaDeCongestionamentoAnterior() )
                {

                    if ( udp.getTamanhoDeJanelaDeRepeticaoSeletiva() > udp.getJanelaDeCongestionamento() ) 
                    {
                        udp.incrementeJanelaDeCongestionamento();
                    }

                    System.out.println( "JANELA DE CONGESTIONAMENTO - " + udp.getJanelaDeCongestionamento() );

                    int base = udp.getProxNumDaSequenciaDePacotes();
                    int janelaAtual = -1;
    
                    for ( 
                        int i = udp.getBaseDaJanelaDeCongestionamento();
                        i < udp.getBaseDaJanelaDeCongestionamento() + udp.getJanelaDeCongestionamento(); 
                        i++
                    ) {
                        
                        if (
                            ! this.acabouCriacaoDePacotes
                            && udp.getProxNumDaSequenciaDePacotes()
                            < udp.getBaseDeEnvio() + udp.getTamanhoDeJanelaDeRepeticaoSeletiva()
                        )
                        {
    
                            byte[] pacoteParaEnvio = preparePacote( streamDaMensagem );
    
                            udp.enviePacote( pacoteParaEnvio );
    
                            System.out.println( 
                                udp.getDenominacao() 
                                + ": Envio do pacote " 
                                + udp.getProxNumDaSequenciaDePacotes() 
                            );
    
                            udp.adicionarACKaReceber( udp.getProxNumDaSequenciaDePacotes() );
    
                            udp.adicionarTimeout( udp.getProxNumDaSequenciaDePacotes() , pacoteParaEnvio );
    
                            udp.incrementeProxNumDaSequenciaDePacotes();
    
                        }
                        else
                        {
                            janelaAtual = i;
                            break;
                        }
                    
                    }

                    if ( janelaAtual == -1 )
                    {
                        janelaAtual = udp.getJanelaDeCongestionamento();
                    }

                    udp.configureBaseDaJanelaDeCongestionamento( base, janelaAtual );
    
                    System.out.println( "JANELA DE CONGESTIONAMENTO - FIM - " + base + " " + janelaAtual );

                }

                udp.getSemaforoDeTimeouts().release();
                udp.getSemaforoDeFluxo().release();
                udp.getSemaforoDeReenvios().release();
                udp.getSemaforoDeCongestionamento().release();

                sleep( 5 );























                

                // if (
                //     (
                //         ! udp.existemPacotesEmTimeout()
                //         && ! udp.verificarACKdaJanelaDeCongestionamentoAnterior()
                //     )
                // ) {

                //     if ( udp.getProxNumDaSequenciaDePacotes() != 0 ) 
                //     {
                //         udp.getSemaforoDasVars().release();
                //         continue;
                //     }
                    
                // }

                // if ( udp.existemPacotesEmTimeout() )
                // {

                //     udp.reduzaJanelaDeCongestionamento();
    
                //     System.out.println( 
                //         udp.getDenominacao() 
                //         + ": ------------------------------------------ TIMEOUT "
                //         + udp.getJanelaDeCongestionamento()
                //     );

                //     Entry<Integer, byte[]> pacote = udp.obterPacoteEmTimeout();

                //     udp.enviePacote( pacote.getValue() );

                //     System.out.println( 
                //         udp.getDenominacao()
                //         + ": Re-envio do pacote em timeout " 
                //         + pacote.getKey()
                //     );

                //     udp.adicioneTimeout( 
                //         pacote.getKey(),
                //         pacote.getValue()
                //     );

                // } 
                // else if ( 
                //     udp.verificarACKdaJanelaDeCongestionamentoAnterior() 
                //     || ! primeiroEnvioRealizado
                // )
                // {

                //     if ( this.acabouCriacaoDePacotes )
                //     {
                //         continue;
                //     }

                //     if ( udp.getTamanhoDeJanelaDePacotes() > udp.getJanelaDeCongestionamento() ) 
                //     {
                //         udp.incrementeJanelaDeCongestionamento();
                //     }

                //     udp.configureBaseDaJanelaDeCongestionamento( 
                //         udp.getProxNumDaSequenciaDePacotes(),
                //         udp.getJanelaDeCongestionamento()
                //     );
    
                //     System.out.println( 
                //         udp.getDenominacao() 
                //         + ": ------------------------------------------ INICIO DE JANELA "
                //         + udp.getJanelaDeCongestionamento()
                //     );
    
                //     for ( 
                //         int i = udp.getBaseDaJanelaDeCongestionamento(); 
                //         i < udp.getBaseDaJanelaDeCongestionamento() + udp.getJanelaDeCongestionamento(); 
                //         i++
                //     ) {
    
                //         if (
                //             ! this.acabouCriacaoDePacotes
                //             && udp.getProxNumDaSequenciaDePacotes()
                //             < udp.getBaseDeEnvio() + udp.getTamanhoDeJanelaDePacotes()
                //         )
                //         {
        
                //             byte[] pacoteParaEnvio = preparePacote( streamDaMensagem );
        
                //             udp.enviePacote( pacoteParaEnvio );

                //             primeiroEnvioRealizado = true;
        
                //             System.out.println( 
                //                 udp.getDenominacao() 
                //                 + ": Envio do pacote " 
                //                 + udp.getProxNumDaSequenciaDePacotes() 
                //             );
        
                //             udp.adicioneTimeout( 
                //                 udp.getProxNumDaSequenciaDePacotes(),
                //                 pacoteParaEnvio
                //             );
        
                //             udp.incrementeProxNumDaSequenciaDePacotes();
        
                //         }
                        
                //     }
        
                //     System.out.println( 
                //         udp.getDenominacao() 
                //         + ": ------------------------------------------ FIM DE JANELA"
                //     );

                // }

                // udp.getSemaforoDasVars().release();
                
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