package implementacoes_udp.servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import criptografia.CriptografiaAES;
import modelos.EnderecoDeMaquina;

public class UDPdoServidor
    extends Thread
{
    
    private final int idDoServidor;

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;

    private DatagramSocket socket;

    private Semaphore semaforoDeFluxo;
    private SortedMap<Integer,Integer> baseDeRecebimento;

    private SortedMap<Integer,String> mensagensDosCliente;
    private SortedMap<Integer,Integer> tamanhoDosBuffersDeMensagens;
    private SortedMap<Integer,SortedMap<Integer,String>> bufferDeMsgsRecebidasDosClientes;

    private int tamanhoDoPacote;
    private int tamanhoDaJanelaDeRepeticaoSeletiva;
    private int atrasoDeRecepcao;
    
    private int atrasoDePropagacao;
    private int atrasoDeTransmissao;
    private double probabilidadeDePerda;

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
        int portaDoServidor,
        EnderecoDeMaquina roteador,
        SortedMap<Integer,EnderecoDeMaquina> clientes
    )
        throws Exception
    {

        this.idDoServidor = 0;

        this.servidor = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(), 
            portaDoServidor
        );

        this.roteador = roteador;

        this.clientes = clientes;

        this.tamanhoDoPacote = 1000;

        this.atrasoDePropagacao = 0;
        this.atrasoDeTransmissao = 0;
        this.probabilidadeDePerda = 0;

        this.numAnteriorDaSequenciaDePacotes = -1;
        this.proxNumDaSequenciaDePacotes = 0;

        this.tamanhoDosBuffersDeMensagens = new TreeMap<Integer,Integer>();
        this.mensagensDosCliente = new TreeMap<Integer,String>();
        this.bufferDeMsgsRecebidasDosClientes = new TreeMap<Integer,SortedMap<Integer,String>>();

        this.bufferDePacotes = new LinkedList<DatagramPacket>();

        this.semaforoDeFluxo = new Semaphore( 1 );
        this.baseDeRecebimento = new TreeMap<Integer,Integer>();
        this.tamanhoDaJanelaDeRepeticaoSeletiva = 10;

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

    public void setTamanhoDaJanelaDeRepeticaoSeletiva ( int tamanhoDaJanelaDeRepeticaoSeletiva )
    {
        this.tamanhoDaJanelaDeRepeticaoSeletiva = tamanhoDaJanelaDeRepeticaoSeletiva;
    }

    public void setAtrasoDeRecepcao ( int atrasoDeRecepcao )
    {
        this.atrasoDeRecepcao = atrasoDeRecepcao;
    }

    public void setAtrasoDePropagacao ( int atrasoDePropagacao )
    {
        this.atrasoDePropagacao = atrasoDePropagacao;
    }

    public void setAtrasoDeTransmissao ( int atrasoDeTransmissao )
    {
        this.atrasoDeTransmissao = atrasoDeTransmissao;
    }

   public void setProbabilidadeDePerda ( double probabilidadeDePerda )
    {
        this.probabilidadeDePerda = probabilidadeDePerda;
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

    SortedMap<Integer,EnderecoDeMaquina> getClientes () 
    {
        return this.clientes;
    }

    int getTamanhoDoPacote () 
    {
        return this.tamanhoDoPacote;
    }

    int getTamanhoDaJanelaDeRepeticaoSeletiva () 
    {
        return this.tamanhoDaJanelaDeRepeticaoSeletiva;
    }

    int getAtrasoDeTransmissao () 
    {
        return this.atrasoDeTransmissao;
    }

    int getNumAnteriorDaSequenciaDePacotes () 
    {
        return this.numAnteriorDaSequenciaDePacotes;
    }

    int getProxNumDaSequenciaDePacotes () 
    {
        return this.proxNumDaSequenciaDePacotes;
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
     *      M??TODOS IMPLEMENTADOS
     * 
     */

    boolean verificarAbaixoDaJanelaDeRepeticao( int idDoCliente, int numDoPacote ) 
    {
        return this.baseDeRecebimento.get( idDoCliente ) > numDoPacote;
    }

    boolean verificarJanelaDeRepeticaoSeletiva( int idDoCliente, int numDoPacote ) 
    {

        if ( ! this.baseDeRecebimento.containsKey( idDoCliente ) )
        {
            this.baseDeRecebimento.put( idDoCliente, 0 );
        }
        
        if ( 
            numDoPacote < this.baseDeRecebimento.get( idDoCliente )
            || numDoPacote > this.baseDeRecebimento.get( idDoCliente ) + tamanhoDaJanelaDeRepeticaoSeletiva
        ) 
        {
            return false;
        }

        if ( numDoPacote == this.baseDeRecebimento.get( idDoCliente ) )
        {

            this.baseDeRecebimento.put( 
                idDoCliente, 
                this.baseDeRecebimento.get( idDoCliente ) + 1 
            );
            
            boolean foiAtualizado = false;
            while ( ! foiAtualizado ) 
            {
            
                if ( 
                        
                    this.bufferDeMsgsRecebidasDosClientes
                        .containsKey( idDoCliente )
                    && this.bufferDeMsgsRecebidasDosClientes
                        .get( idDoCliente )
                        .containsKey( this.baseDeRecebimento.get( idDoCliente ) )
                )
                {

                    this.baseDeRecebimento.put( 
                        idDoCliente, 
                        this.baseDeRecebimento.get( idDoCliente ) + 1 
                    );

                }
                else
                {
                    foiAtualizado = true;
                }
            
            }

        }

        return true;

    }

    void adicionarPacoteAoBuffer( DatagramPacket pacote )
        throws Exception
    {
        if ( this.atrasoDeRecepcao > 0 )
        {
            sleep( this.atrasoDeRecepcao );
        }
        this.bufferDePacotes.add( pacote );
    }

    DatagramPacket removerPacoteDoBuffer ()
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

    void adicionarMensagemAoBuffer (
        int idDoCliente,
        int numDoPacote,
        String mensagem 
    )
    {

        if ( ! this.bufferDeMsgsRecebidasDosClientes.containsKey( idDoCliente ) ) 
        {
            SortedMap<Integer,String> listaDeMsgsDosPacotesDoCliente = new TreeMap<>();
            this.bufferDeMsgsRecebidasDosClientes.put(
                idDoCliente, 
                listaDeMsgsDosPacotesDoCliente
            );
        }

        SortedMap<Integer,String> listaDeMsgs = this.bufferDeMsgsRecebidasDosClientes.get( idDoCliente );
        listaDeMsgs.put( numDoPacote, mensagem );

    }

    void salvarTamanhoDoBufferDeMensagem( int idDoCliente, int tamanhoDoBuffer )
    {
        this.tamanhoDosBuffersDeMensagens.put( idDoCliente, tamanhoDoBuffer );
    }

    boolean verificarSeAbaixoDoBufferCompleto ( int idDoCliente ) 
    {
        if ( this.tamanhoDosBuffersDeMensagens.containsKey( idDoCliente ) )
        {
            return 
                this.bufferDeMsgsRecebidasDosClientes.get( idDoCliente ).size() 
                == this.tamanhoDosBuffersDeMensagens.get( idDoCliente );
        }
        else return false;
    }

    void salvarMensagem( int idDoCliente )
        throws Exception
    {

        SortedMap<Integer,String> listaDeMsgsDoCliente =
            this.bufferDeMsgsRecebidasDosClientes.get( idDoCliente );

        StringBuffer bufferDeMsg = new StringBuffer();

        for ( String msg : listaDeMsgsDoCliente.values() ) 
        {
            bufferDeMsg.append(msg);
        }

        this.mensagensDosCliente.put( 
            idDoCliente, 
            this.criptografia
                .decodificarMensagem(
                    bufferDeMsg.toString()
                ) 
        );

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

        sleep( this.atrasoDeTransmissao );

        if( 
            Math.random() < ( 1 - this.probabilidadeDePerda )
        )
        {
            this.socket.send( pacoteDeEnvio );
        }

    }

    public void run () 
    {

        try
        {

            this.criptografia = new CriptografiaAES();

            this.socket = new DatagramSocket( this.servidor.getPorta() );
            
            this.threadDeSaida = new ThreadDeSaida( this );
            this.threadDeEntrada = new ThreadDeEntrada( this, this.atrasoDePropagacao );

            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            System.out.println( this.servidor.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) 
        {    
            e.printStackTrace();
            System.exit( -1 );   
        }

    }

}
