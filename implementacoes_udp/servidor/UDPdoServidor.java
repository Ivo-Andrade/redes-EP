package implementacoes_udp.servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import criptografia.CriptografiaAES;
import modelos.EnderecoDeMaquina;

public class UDPdoServidor
    extends Thread
{
    
    private final int idDoServidor;

    private final EnderecoDeMaquina servidor;
    private EnderecoDeMaquina roteador;

    private DatagramSocket socket;

    private SortedMap<Integer,String> clientes;
    private SortedMap<Integer,String> mensagensDosCliente;
    private SortedMap<Integer,SortedMap<Integer,String>> bufferDePacotesRecebidosDosClientes;

    private int tamanhoDoPacote = 1000;
    private int tamanhoDeJanelaDePacotes = 10;

    private int atrasoDePropagacao = 0;
    private int probabilidadeDePerda = 0;

    private LinkedList<DatagramPacket> bufferDePacotes;

    private int numAnteriorDaSequenciaDePacotes;
    private int proxNumDaSequenciaDePacotes;

    private boolean aTransferenciaTerminou;

    private ThreadDeEntrada threadDeEntrada;
    private ThreadDeSaida threadDeSaida;

    private CriptografiaAES criptografia;

    /**
     * 
     *      CONSTRUTOR
     * 
     */

    public UDPdoServidor (
        String denominacao,
        int portaDoServidor
    )
        throws Exception
    {

        this.idDoServidor = 0;

        this.servidor = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(), 
            portaDoServidor
        );

        this.roteador = 
            new EnderecoDeMaquina(
                "simulacao_base-Roteador", 
                InetAddress.getLocalHost(), 
                9555
            );

        this.numAnteriorDaSequenciaDePacotes = -1;
        this.proxNumDaSequenciaDePacotes = 0;

        this.clientes = new TreeMap<Integer,String>();

        this.mensagensDosCliente = new TreeMap<Integer,String>();
        this.bufferDePacotesRecebidosDosClientes = new TreeMap<Integer,SortedMap<Integer,String>>();

        this.bufferDePacotes = new LinkedList<DatagramPacket>();

    }

    /**
     * 
     *      SETTERS
     * 
     */

    public void setClientes (
        SortedMap<Integer,String> listaClientes
    )
    {
        this.clientes = listaClientes;
    }

    public void setRoteador ( 
        EnderecoDeMaquina roteador
    )
    {
        this.roteador = roteador;
    }

    public void setTamanhoDoPacote ( Integer tamanhoDoPacote )
    {
        if ( tamanhoDoPacote != null )
        {
            this.tamanhoDoPacote = tamanhoDoPacote;
        }
    }

    public void setTamanhoDeJanelaDePacotes ( Integer tamanhoDeJanelaDePacotes )
    {
        if ( tamanhoDeJanelaDePacotes != null )
        {
            this.tamanhoDeJanelaDePacotes = tamanhoDeJanelaDePacotes;
        }
    }

    public void setAtrasoDePropagacao ( Integer atrasoDePropagacao )
    {
        if ( atrasoDePropagacao != null )
        {
            this.atrasoDePropagacao = atrasoDePropagacao;
        }
    }

   public void setProbabilidadeDePerda ( Integer probabilidadeDePerda )
    {
        if ( probabilidadeDePerda != null )
        {
            this.probabilidadeDePerda = probabilidadeDePerda;
        }
    }

    /**
     * 
     *      GETTERS
     * 
     */

    int getIdDoServidor () 
    {
        return this.idDoServidor;
    }

    String getDenominacao () 
    {
        return this.servidor.getNome();
    }

    DatagramSocket getSocket ()
    {
        return this.socket;
    }

    InetAddress getEnderecoIPdoServidor ()
    {
        return this.servidor.getEnderecoIP();
    }

    int getPortaDoServidor () 
    {
        return this.servidor.getPorta();
    }

    SortedMap<Integer,String> getClientes () 
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

    int getAtrasoDePropagacao () 
    {
        return this.atrasoDePropagacao;
    }

    int getNumAnteriorDaSequenciaDePacotes () 
    {
        return this.numAnteriorDaSequenciaDePacotes;
    }

    int getProxNumDaSequenciaDePacotes () 
    {
        return this.proxNumDaSequenciaDePacotes;
    }

    int getProbabilidadeDePerda () 
    {
        return this.probabilidadeDePerda;
    }

    boolean getATransferenciaTerminou () 
    {
        return this.aTransferenciaTerminou;
    }

    CriptografiaAES getCriptografia () 
    {
        return this.criptografia;
    }

    /**
     * 
     *      MÉTODOS IMPLEMENTADOS
     * 
     */

    public void adicionarPacoteAoBuffer( DatagramPacket pacote ) 
    {
        if ( this.bufferDePacotes.size() < tamanhoDeJanelaDePacotes )
        {
            this.bufferDePacotes.add( pacote );
        }
    }

    public DatagramPacket removerPacoteDoBuffer ()
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

    void sinalizarTerminoDaTransferencia () 
    {
        this.aTransferenciaTerminou = true;
    }

    void atualizeContadoresDaSequenciaDePacotes () 
    {
        this.proxNumDaSequenciaDePacotes++;
        this.numAnteriorDaSequenciaDePacotes = this.proxNumDaSequenciaDePacotes;
    }

    void enviarPacote ( byte[] pacote )
        throws Exception
    {
                        
        DatagramPacket pacoteDeEnvio =
            new DatagramPacket(
                pacote, 
                pacote.length,
                this.roteador.getEnderecoIP(),
                this.roteador.getPorta()
            );

        if ( this.atrasoDePropagacao > 0 ) 
        {
            sleep( 
                this.atrasoDePropagacao
                * pacote.length
            );
        }

        if( 
            Math.random() < ( 1 - this.probabilidadeDePerda )
        )
        {
            this.socket.send( pacoteDeEnvio );
        }

    }

    public void run () {

        try
        {

            this.criptografia = new CriptografiaAES();

            this.socket = new DatagramSocket( this.servidor.getPorta() );
            
            this.threadDeSaida = new ThreadDeSaida( this );
            this.threadDeEntrada = new ThreadDeEntrada( this );

            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            System.out.println( this.servidor.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) {
            
            e.printStackTrace();
            System.exit( -1 );
            
        }

    }

}