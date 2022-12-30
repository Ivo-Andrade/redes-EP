package implementacoes_udp.servidor;

import java.net.DatagramPacket;

import pacotes.GerenciadorDePacote;

public class ThreadDeEntrada 
    extends Thread
{

    private final UDPdoServidor udp;
    private final int atrasoDePropagacao;

    public ThreadDeEntrada ( 
        UDPdoServidor udp, 
        int atrasoDePropagacao
    ) 
    { 
        this.udp = udp;
        this.atrasoDePropagacao = atrasoDePropagacao;
    }

    public void run ()
    {

        try {

            while ( true ) 
            {
                
                byte[] dadosDeEntrada = new byte[ udp.getTamanhoDoPacote() * 2 ];
                DatagramPacket pacoteDeEntrada = 
                    new DatagramPacket( dadosDeEntrada, dadosDeEntrada.length );

                udp.getSocket().receive( pacoteDeEntrada );

                if ( this.atrasoDePropagacao > 0 )
                {
                    sleep( this.atrasoDePropagacao );
                }
                
                if ( GerenciadorDePacote.verificarPacote( dadosDeEntrada, pacoteDeEntrada.getLength() ) )
                {
                    udp.adicionarPacoteAoBuffer( pacoteDeEntrada );
                }

            }

        } catch ( Exception e ) 
        {
            e.printStackTrace();
            System.exit(-1);
        }

    }
    
}