package implementacoes_udp.roteador;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import modelos.EnderecoDeMaquina;

public class UDPdeRoteador 
    extends Thread
{

    private DatagramSocket socket;

    private final EnderecoDeMaquina roteador;
    private EnderecoDeMaquina servidor;
    private SortedMap<Integer,EnderecoDeMaquina> clientes;

    private int tamanhoDoPacote = 1000;
    private int tamanhoDeJanelaDePacotes = 10;

    private SortedMap<Integer,Integer> atrasoDePropagacaoNoEnvioParaMaquinas;
    private SortedMap<Integer,Integer> probabilidadeDePerdaNoEnvioParaMaquinas;

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
        int portaDoRoteador
    )
        throws Exception
    {

        this.roteador = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(), 
            portaDoRoteador
        );

        this.servidor = 
            new EnderecoDeMaquina(
                "simulacao_base-Servidor", 
                InetAddress.getLocalHost(), 
                9999
            );

        this.clientes = new TreeMap<Integer,EnderecoDeMaquina>();

        this.atrasoDePropagacaoNoEnvioParaMaquinas = new TreeMap<Integer,Integer>();
        this.probabilidadeDePerdaNoEnvioParaMaquinas = new TreeMap<Integer,Integer>();

        this.bufferDePacotes = new LinkedList<DatagramPacket>();

    }

    /**
     * 
     *      SETTERS
     * 
     */

    public void setClientes (
        SortedMap<Integer,EnderecoDeMaquina> listaClientes
    )
    {
        this.clientes = listaClientes;
    }

    public void setServidor ( 
        EnderecoDeMaquina enderecoDeMaquina
    )
    {
        this.servidor = enderecoDeMaquina;
    }

    public void setTamanhoDoPacote ( Integer tamanhoDoPacote )
    {
        if ( tamanhoDoPacote != null )
        {
            this.tamanhoDoPacote = tamanhoDoPacote;
        }
    }

    public void setTamanhoDeJanela ( Integer tamanhoDeJanela )
    {
        if ( tamanhoDeJanela != null )
        {
            this.tamanhoDeJanelaDePacotes = tamanhoDeJanela;
        }        
    }

    public void setAtrasoDePropagacao ( SortedMap<Integer,Integer> atrasoDePropagacao )
    {
        if ( atrasoDePropagacao != null )
        {
            this.atrasoDePropagacaoNoEnvioParaMaquinas = atrasoDePropagacao;
        }
    }

    public void setProbabilidadeDePerda ( SortedMap<Integer,Integer> probabilidadeDePerda )
    {
        if ( probabilidadeDePerda != null )
        {
            this.probabilidadeDePerdaNoEnvioParaMaquinas = probabilidadeDePerda;
        }
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

        if ( this.atrasoDePropagacaoNoEnvioParaMaquinas.get( idMaquina ) > 0 ) 
        {
            sleep( 
                this.atrasoDePropagacaoNoEnvioParaMaquinas.get( idMaquina )
                * length
            );
        }

        if( 
            Math.random() < ( 1 - this.probabilidadeDePerdaNoEnvioParaMaquinas.get( idMaquina ) )
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
            this.threadDeEntrada = new ThreadDeEntrada( this );
    
            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            System.out.println( this.roteador.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) {
            
            e.printStackTrace();
            System.exit( -1 );
            
        }
    
    }
    
}
