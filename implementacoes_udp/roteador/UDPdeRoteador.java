package implementacoes_udp.roteador;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import modelos.EnderecoDeMaquina;

public class UDPdeRoteador 
    extends Thread
{

    private DatagramSocket socket;

    private final EnderecoDeMaquina roteador;
    private final EnderecoDeMaquina servidor;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;

    private int tamanhoDoPacote;
    private int tamanhoDeJanelaDePacotes;

    private final SortedMap<Integer,Integer> atrasosDePropagacao;
    private final SortedMap<Integer,Integer> atrasosDeTransmissao;
    private final SortedMap<Integer,Integer> probabilidadesDePerda;

    private LinkedList<DatagramPacket> bufferDePacotes;

    private ThreadDeEntrada threadDeEntrada;
    private ThreadDeSaida threadDeSaida;

    /**
     * 
     *      CONSTRUTOR
     * 
     */

    public UDPdeRoteador (
        String denominacao,
        int portaDoRoteador,
        EnderecoDeMaquina servidor,
        SortedMap<Integer,EnderecoDeMaquina> clientes,
        SortedMap<Integer,Integer> atrasosDePropagacao,
        SortedMap<Integer,Integer> atrasosDeTransmissao,
        SortedMap<Integer,Integer> probabilidadesDePerda
    )
        throws Exception
    {

        this.roteador = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(), 
            portaDoRoteador
        );

        this.tamanhoDoPacote = 1000;
        this.tamanhoDeJanelaDePacotes = 10;

        this.servidor = servidor;
        this.clientes = clientes;

        this.atrasosDePropagacao = atrasosDePropagacao;
        this.atrasosDeTransmissao = atrasosDeTransmissao;
        this.probabilidadesDePerda = probabilidadesDePerda;

        this.bufferDePacotes = new LinkedList<DatagramPacket>();

    }

    /**
     * 
     *      SETTERS
     * 
     */

    public void setTamanhoDoPacote ( int tamanhoDoPacote )
    {
        this.tamanhoDoPacote = tamanhoDoPacote;
    }

    public void setTamanhoDeJanela ( int tamanhoDeJanela )
    {
        this.tamanhoDeJanelaDePacotes = tamanhoDeJanela;
    }

    /**
     * 
     *      GETTERS
     * 
     */

    String getDenominacao () 
    {
        return this.roteador.getNome();
    }

    DatagramSocket getSocket ()
    {
        return this.socket;
    }

    InetAddress getEnderecoIPdoRoteador () 
    {
        return this.roteador.getEnderecoIP();
    }

    public String getNomeDoServidor() 
    {
        return this.servidor.getNome();
    }

    InetAddress getEnderecoIPdoServidor () 
    {
        return this.servidor.getEnderecoIP();
    }

    int getPortaDoServidor () 
    {
        return this.servidor.getPorta();
    }

    SortedMap<Integer,EnderecoDeMaquina> getClientes () 
    {
        return this.clientes;
    }

    int getTamanhoDoPacote () 
    {
        return this.tamanhoDoPacote;
    }

    int getTamanhoDeJanelaDePacotes () 
    {
        return this.tamanhoDeJanelaDePacotes;
    }
    
    /**
     * 
     *      MÉTODOS IMPLEMENTADOS
     * 
     */

    public boolean existePacotesNoBuffer() 
    {
        return ( this.bufferDePacotes.size() > 0 );
    }

    public void adicionarPacoteAoBuffer( DatagramPacket pacote ) 
    {
        if ( this.bufferDePacotes.size() < tamanhoDeJanelaDePacotes )
        {
            this.bufferDePacotes.add( pacote );
        }
    }

    public DatagramPacket removerPacoteDoBuffer ()
        throws Exception
    {
        if ( this.bufferDePacotes.size() > 0 )
        {
            try 
            {
                return this.bufferDePacotes.removeFirst();
            } 
            catch ( NoSuchElementException e ) 
            {
                // SKIP BUFFER READ
            }
        }
        return null;
    }

    public void enviePacoteAoCliente ( 
        int idDoCliente,
        byte[] pacoteParaCliente 
    )
        throws Exception
    {

        EnderecoDeMaquina cliente = this.clientes.get( idDoCliente );
                        
        DatagramPacket pacoteDeEnvio =
            new DatagramPacket(
                pacoteParaCliente, 
                pacoteParaCliente.length,
                cliente.getEnderecoIP(),
                cliente.getPorta()
            );

        enviarPacote( idDoCliente, pacoteDeEnvio, pacoteParaCliente.length );

    }

    public void enviePacoteAoServidor( byte[] pacoteParaServidor )
        throws Exception
    {
                        
        DatagramPacket pacoteDeEnvio =
            new DatagramPacket(
                pacoteParaServidor, 
                pacoteParaServidor.length,
                this.servidor.getEnderecoIP(),
                this.servidor.getPorta()
            );

        enviarPacote( 0, pacoteDeEnvio, pacoteParaServidor.length );

    }

    void enviarPacote (
        int idMaquina,
        DatagramPacket pacoteDeEnvio, 
        int length 
    )
        throws Exception
    {

        if ( this.atrasosDeTransmissao.get( idMaquina ) > 0 ) 
        {
            sleep( 
                this.atrasosDeTransmissao.get( idMaquina )
                * length
            );
        }

        if( 
            Math.random() < ( 1 - this.probabilidadesDePerda.get( idMaquina ) )
        )
        {
            this.socket.send( pacoteDeEnvio );
        }

    }

    public void run () 
    {

        try {

            this.socket = new DatagramSocket( this.roteador.getPorta() );
                
            this.threadDeSaida = new ThreadDeSaida( this );
            this.threadDeEntrada = new ThreadDeEntrada( this, this.atrasosDePropagacao );
    
            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            System.out.println( this.roteador.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) 
        {
            
            e.printStackTrace();
            System.exit( -1 );
            
        }
    
    }
    
}
