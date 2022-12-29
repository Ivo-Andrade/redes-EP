package implementacoes_udp.servidor;

import java.net.DatagramPacket;
import java.util.Arrays;

import pacotes.GerenciadorDePacote;

public class ThreadDeSaida 
    extends Thread
{

    private final UDPdoServidor udp;

    public ThreadDeSaida (
        UDPdoServidor udp
    ) 
    { 
        this.udp = udp;
    }
    
    public void run ()
    {

        try {

            while ( true ) {

                DatagramPacket pacote = udp.removerPacoteDoBuffer();

                if ( pacote != null ) 
                {
                    byte[] dadosDoPacote = Arrays.copyOfRange(
                        pacote.getData(), 
                        0, 
                        pacote.getLength()
                    );

                    int idDoCliente = 
                        GerenciadorDePacote
                            .decodificarIdDoRemetente( dadosDoPacote );

                    int numDoPacote = 
                        GerenciadorDePacote
                            .decodificarNumDoPacote( dadosDoPacote );

                    System.out.println(
                        udp.getDenominacao()
                        + ": Pacote " 
                        + numDoPacote
                        + " recebido de " 
                        + udp.getClientes().get( idDoCliente ) 
                    );

                    byte[] bytesDoPayload = 
                        GerenciadorDePacote
                            .decodificarDados( dadosDoPacote );

                    if ( bytesDoPayload.length == 0 )
                    {

                        byte[] pacoteDeACK = 
                            GerenciadorDePacote
                                .construirPacote(
                                    udp.getIdDoServidor(),
                                    idDoCliente,
                                    -2,
                                    new byte[0]
                                );

                        udp.enviarPacote( pacoteDeACK );

                        System.out.println(
                            udp.getDenominacao()
                            + ": Envio de ACK " 
                            + (-2)
                            + " para "
                            + udp.getClientes().get( idDoCliente ) 
                        );

                        // TODO: WRITE FILE FROM CLIENT, MAYBE

                    }
                    else
                    {

                        // TODO: SAVE MSG FROM CLIENT, MAYBE
                        // using bytesDoPayload

                        byte[] pacoteDeACK = 
                            GerenciadorDePacote
                                .construirPacote(
                                    udp.getIdDoServidor(),
                                    idDoCliente,
                                    numDoPacote,
                                    new byte[0]
                                );

                        udp.enviarPacote( pacoteDeACK );

                        System.out.println(
                            udp.getDenominacao()
                            + ": Envio de ACK " 
                            + numDoPacote
                            + " para "
                            + udp.getClientes().get( idDoCliente )
                        );

                    }
                            
                }

                sleep( 1 );

            }
            
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
    
}                
