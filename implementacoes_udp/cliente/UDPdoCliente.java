package implementacoes_udp.cliente;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.AbstractMap;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import criptografia.CriptografiaAES;
import modelos.EnderecoDeMaquina;

public class UDPdoCliente 
    extends Thread 
{
    
    private final int idDoServidor;
    private final int idDeCliente;
    private final String mensagemDeEnvio;

    private final EnderecoDeMaquina cliente;
    private final EnderecoDeMaquina roteador;

    private DatagramSocket socket;
    
    private int tamanhoDoPacote;
    private int tamanhoDeJanelaDePacotes;
    
    private int atrasoDePropagacao;
    private int atrasoDeTransmissao;
    private double probabilidadeDePerda;

    private int tempoDeTimeoutDePacote;
    
    private Semaphore semaforoDasVars;

    private int janelaDeCongestionamento;
    private int baseDaJanelaDeCongestionamento;
    private int limiteDePartidaLenta;
    
    private int baseDeEnvio;
    private int proxNumDaSequenciaDePacotes;
    private SortedMap<Integer,Boolean> listaDeACKdePacotes;

    private SortedMap<Integer,Timer> listaDeTimersCorrentes;
    private SortedMap<Integer,TimerTask> listaDeTimerTasksCorrentes;
    private SortedMap<Integer,byte[]> listaDePacotesEmTimeout;

    private boolean aTransferenciaTerminou;

    private ThreadDeEntrada threadDeEntrada;
    private ThreadDeSaida threadDeSaida;

    private CriptografiaAES criptografia;

    /**
     * 
     *      CONSTRUTOR
     * 
     */

    public UDPdoCliente (
        int idDeCliente,
        String denominacao,
        int portaCliente,
        String mensagemDeEnvio,
        EnderecoDeMaquina roteador        
    )
        throws Exception
    {

        this.idDoServidor = 0;

        this.idDeCliente = idDeCliente;
        this.mensagemDeEnvio = mensagemDeEnvio;

        this.cliente = new EnderecoDeMaquina(
            denominacao, 
            InetAddress.getLocalHost(),
            portaCliente 
        );
        
        this.tamanhoDoPacote = 1000;
        this.tamanhoDeJanelaDePacotes = 10;

        this.janelaDeCongestionamento = 0;
        this.baseDaJanelaDeCongestionamento = 0;
        this.limiteDePartidaLenta = Integer.MAX_VALUE;
    
        this.atrasoDePropagacao = 0;
        this.atrasoDeTransmissao = 0;
        this.probabilidadeDePerda = 0;

        this.tempoDeTimeoutDePacote = 300;

        this.roteador = roteador;

        this.baseDeEnvio = 0;
        this.proxNumDaSequenciaDePacotes = 0;

        this.listaDeACKdePacotes = new TreeMap<Integer,Boolean>();
        for ( int i = baseDeEnvio; i < this.tamanhoDeJanelaDePacotes; i++ ) 
        {
            this.listaDeACKdePacotes.put( i, false );
        }

        this.listaDeTimersCorrentes = new TreeMap<Integer,Timer>();
        this.listaDeTimerTasksCorrentes = new TreeMap<Integer,TimerTask>();
        this.listaDePacotesEmTimeout = new TreeMap<Integer,byte[]>();

        this.semaforoDasVars = new Semaphore( 1 );

        this.aTransferenciaTerminou = false;

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

    public void setTamanhoDeJanelaDePacotes ( int tamanhoDeJanela )
    {
        this.tamanhoDeJanelaDePacotes = tamanhoDeJanela;

        this.listaDeACKdePacotes = new TreeMap<Integer,Boolean>();
        for ( int i = baseDeEnvio; i < this.tamanhoDeJanelaDePacotes; i++ ) 
        {
            this.listaDeACKdePacotes.put( i, false );
        }
    }

    public void setTempoDeTimeout ( int tempoDeTimeout )
    {
        this.tempoDeTimeoutDePacote = tempoDeTimeout;
    }
    
    public void setAtrasoDePropagacao( int atrasoDePropagacao ) 
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

    int getIdDeCliente () 
    {
        return this.idDeCliente;
    }

    int getIdDoServidor () 
    {
        return this.idDoServidor;
    }

    String getDenominacao () 
    {
        return this.cliente.getNome();
    }

    DatagramSocket getSocket ()
    {
        return this.socket;
    }

    String getMensagemDeEnvio () 
    {
        return this.mensagemDeEnvio;
    }

    InetAddress getEnderecoIPdoCliente () 
    {
        return this.cliente.getEnderecoIP();
    }

    InetAddress getEnderecoIPdoRoteador () 
    {
        return this.roteador.getEnderecoIP();
    }

    int getPortaDoRoteador () 
    {
        return this.roteador.getPorta();
    }

    int getTamanhoDoPacote () 
    {
        return this.tamanhoDoPacote;
    }

    int getTamanhoDeJanelaDePacotes () 
    {
        return this.tamanhoDeJanelaDePacotes;
    }

    int getJanelaDeCongestionamento () 
    {
        return this.janelaDeCongestionamento;
    }

    int getBaseDaJanelaDeCongestionamento () 
    {
        return this.baseDaJanelaDeCongestionamento;
    }

    int getTempoDeTimeoutDePacote () 
    {
        return this.tempoDeTimeoutDePacote;
    }

    int getAtrasoDeTransmissao () 
    {
        return this.atrasoDeTransmissao;
    }

    int getBaseDeEnvio () 
    {
        return this.baseDeEnvio;
    }

    int getProxNumDaSequenciaDePacotes () 
    {
        return this.proxNumDaSequenciaDePacotes;
    }
    
    Semaphore getSemaforoDasVars () 
    {
        return this.semaforoDasVars;
    }
    
    boolean aTransferenciaTerminou () 
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

    void configureBaseDaJanelaDeCongestionamento ( 
        int base,
        int janelaDeCongestionamento
    ) 
    {
        this.baseDaJanelaDeCongestionamento = base;
        this.janelaDeCongestionamento = janelaDeCongestionamento;
    }

    boolean verificarACKdaJanelaDeCongestionamentoAnterior ()
        throws Exception
    {
        
        if (
            this.listaDeACKdePacotes.firstKey() > this.baseDaJanelaDeCongestionamento
        )
        {
            return true;
        }
    
        for ( int numPacote : this.listaDeACKdePacotes.keySet() )
        {

            if (
                numPacote >= this.baseDaJanelaDeCongestionamento
                && numPacote < this.baseDaJanelaDeCongestionamento + this.janelaDeCongestionamento
                && this.listaDeACKdePacotes.get( numPacote ) == true
            )
            {
                return true;
            }
            else if 
            (
                numPacote >= this.baseDaJanelaDeCongestionamento + this.janelaDeCongestionamento
                && this.listaDeACKdePacotes.get( numPacote ) == true
            )
            {
                return true;
            }
            
        }

        return false;

    }

    SortedMap.Entry<Integer,byte[]> obterPacoteEmTimeout ()
        throws Exception
    {

        int numDoPacote = this.listaDePacotesEmTimeout.firstKey();

        byte[] pacote = this.listaDePacotesEmTimeout.remove(
            this.listaDePacotesEmTimeout.firstKey()  
        );

        return new AbstractMap.SimpleEntry<Integer, byte[]>(
            numDoPacote,
            pacote
        );
        
    }

    void incrementeProxNumDaSequenciaDePacotes () 
        throws Exception
    {
        this.proxNumDaSequenciaDePacotes++;
    }

    void atualizarJanela ( int numDeACK ) 
        throws Exception
    {

        if ( this.listaDeACKdePacotes.containsKey( numDeACK ) ) {
            this.listaDeACKdePacotes.put( numDeACK, true );
        }
        else {
            // DISCARD OLD ACK
        }

        if ( numDeACK == this.baseDeEnvio )
        {

            boolean foiAtualizado = false;
            while ( ! foiAtualizado ) 
            {
            
                if ( this.listaDeACKdePacotes.get( this.baseDeEnvio ) == true )
                {

                    this.listaDeACKdePacotes.remove( this.baseDeEnvio );
                    this.listaDeACKdePacotes.put( 
                        this.baseDeEnvio + this.tamanhoDeJanelaDePacotes,
                        false
                    );
                    this.baseDeEnvio++;

                }
                else
                {
                    foiAtualizado = true;
                }
            
            }

        }

    }

    void adicioneTimeout ( int numDoPacote, byte[] pacote )
        throws Exception
    {

        this.listaDeTimersCorrentes.put( numDoPacote, new Timer() );

        this.listaDeTimerTasksCorrentes.put( 
            numDoPacote,
            new TimeoutTask( this, numDoPacote, pacote ) 
        );

        Timer timer = this.listaDeTimersCorrentes.get( numDoPacote );

        timer.schedule( 
            this.listaDeTimerTasksCorrentes.remove( numDoPacote ),
            tempoDeTimeoutDePacote 
        );

    }

    void removeTimeoutTask ( int numPacote )
        throws Exception
    {

        if ( 
            this.listaDeTimersCorrentes.containsKey( numPacote ) 
        )
        {

            TimerTask task = this.listaDeTimerTasksCorrentes.get( numPacote );
            if ( task != null )
            {
                task.cancel();
            }

            Timer timer = this.listaDeTimersCorrentes.get( numPacote );
            timer.cancel();

            this.listaDeTimerTasksCorrentes.remove( numPacote );
            this.listaDeTimersCorrentes.remove( numPacote );

        }

    }

    void enviePacote ( byte[] pacote )
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

            if ( ! socket.isClosed() )
            {
                this.socket.send( pacoteDeEnvio );
            }

        }

    }

    void adicionarNaListaDePacotesEmTimeout ( int numPacote, byte[] pacote )
        throws Exception
    {

        if ( numPacote >= this.baseDeEnvio )
        {
            this.listaDePacotesEmTimeout.put( numPacote, pacote );
        }

    }

    boolean existemPacotesEmTimeout () 
        throws Exception
    {
        return this.listaDePacotesEmTimeout.size() > 0;
    }

    void sinalizarTerminoDaTransferencia () 
    {
        this.aTransferenciaTerminou = true;
    }

    void esvaziarListaDeTimeouts ()
        throws Exception
    {
        this.listaDeTimerTasksCorrentes.forEach( 
            (k, v) -> {
                if ( v != null )
                {
                    v.cancel();
                }
            }
        );
        this.listaDeTimersCorrentes.forEach( 
            (k, v) -> {
                if ( v != null )
                {
                    v.cancel();
                }
            }
        );
        this.listaDeTimerTasksCorrentes.clear();
        this.listaDeTimersCorrentes.clear();
    }

    void incrementeJanelaDeCongestionamento () 
    {

        if ( this.janelaDeCongestionamento == 0 )
        {
            this.janelaDeCongestionamento = 1;
        }
        else if ( this.janelaDeCongestionamento > this.limiteDePartidaLenta )
        {
            this.janelaDeCongestionamento++;
        }
        else
        {
            this.janelaDeCongestionamento*=2;
        }

    }

    void reduzaJanelaDeCongestionamento () 
    {

        int janelaAtual = this.janelaDeCongestionamento;
        this.limiteDePartidaLenta = janelaAtual / 2;
        this.janelaDeCongestionamento = 1;

    }

    public void run () 
    {

        try {

            this.criptografia = new CriptografiaAES();

            this.socket = new DatagramSocket( this.cliente.getPorta() );
                
            this.threadDeSaida = new ThreadDeSaida( this );
            this.threadDeEntrada = new ThreadDeEntrada( this, this.atrasoDePropagacao );
    
            this.threadDeSaida.start();
            this.threadDeEntrada.start();

            System.out.println( this.cliente.getNome() + ": Em funcionamento..." );

        } 
        catch ( Exception e ) 
        {
            
            e.printStackTrace();
            System.exit( -1 );
            
        }
    
    }

}
