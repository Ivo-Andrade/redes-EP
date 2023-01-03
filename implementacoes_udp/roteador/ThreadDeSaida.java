package implementacoes_udp.roteador;

import java.net.DatagramPacket;
import java.util.Arrays;

import pacotes.GerenciadorDePacote;

public class ThreadDeSaida 
    extends Thread
{

    private final UDPdeRoteador udp;

    public ThreadDeSaida (
        UDPdeRoteador udp
    ) 
    {
        this.udp = udp;
    }

    public void run () 
    {

        try {

            while ( true ) 
            {

                DatagramPacket pacote = udp.removerPacoteDoBuffer();

                if ( pacote != null ) 
                {

                    udp.registrarOutputFilaDoRoteador();
                    
                    byte[] dadosDoPacote = Arrays.copyOfRange(
                        pacote.getData(), 
                        0, 
                        pacote.getLength()
                    );

                    int idDoRemetente = 
                        GerenciadorDePacote.decodificarIdDoRemetente( dadosDoPacote );
                    
                    if ( idDoRemetente == 0 )
                    {

                        int idDoDestinatario = 
                            GerenciadorDePacote.decodificarIdDoDestinatario( dadosDoPacote );
                                
                        System.out.println( 
                            udp.getDenominacao()
                                +": Pacote "
                                +  GerenciadorDePacote
                                    .decodificarNumDoPacote( dadosDoPacote )
                                + " recebido de "
                                + udp.getNomeDoServidor()
                                + " para " 
                                + udp.getClientes()
                                    .get( idDoDestinatario )
                                    .getNome()
                            );

                        udp.enviePacoteAoCliente( idDoDestinatario, dadosDoPacote );
                
                    }
                    else
                    {

                        System.out.println(
                            udp.getDenominacao()
                                + ": Pacote "
                                +  GerenciadorDePacote
                                    .decodificarNumDoPacote( dadosDoPacote )
                                + " recebido de " 
                                + udp.getClientes()
                                    .get( idDoRemetente )
                                    .getNome() 
                            );
                        
                        udp.enviePacoteAoServidor( dadosDoPacote );

                    }

                }

                sleep( 5 );

            }
            
        } catch ( Exception e ) 
        {
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
