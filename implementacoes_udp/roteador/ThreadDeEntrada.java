package implementacoes_udp.roteador;

import java.net.DatagramPacket;
import java.util.SortedMap;

import pacotes.GerenciadorDePacote;

public class ThreadDeEntrada 
    extends Thread
{

    private final UDPdeRoteador udp;
    private final SortedMap<Integer, Integer> atrasosDePropagacao;

    public ThreadDeEntrada ( 
        UDPdeRoteador udp, 
        SortedMap<Integer, Integer> atrasosDePropagacao 
    )
    {
        this.udp = udp;
        this.atrasosDePropagacao = atrasosDePropagacao;
    }

    public void run () 
    {

        try {

            while ( true ) 
            {

                byte[] dadosDeEntrada = new byte[ udp.getTamanhoDoPacote() ];
                DatagramPacket pacoteDeEntrada = 
                    new DatagramPacket( dadosDeEntrada, dadosDeEntrada.length );

                udp.getSocket().receive( pacoteDeEntrada );
                
                if ( GerenciadorDePacote.verificarPacote( dadosDeEntrada, pacoteDeEntrada.getLength() ) )
                {

                    int idDoRemetente = GerenciadorDePacote.decodificarIdDoRemetente( dadosDeEntrada );

                    sleep( this.atrasosDePropagacao.get( idDoRemetente ) );

                    udp.adicionarPacoteAoBuffer( pacoteDeEntrada );

                    udp.registrarOutputFilaDoRoteador();
                }

            }

        } catch ( Exception e ) 
        {
            e.printStackTrace();
            System.exit(-1);
        }

    }
    
}
