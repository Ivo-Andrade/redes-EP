package implementacoes_udp.cliente;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    private int tamanhoDeJanelaDeRepeticaoSeletiva;
    
    private int atrasoDePropagacao;
    private int atrasoDeTransmissao;
    private double probabilidadeDePerda;

    private int tempoDeTimeoutDePacote;

    private Semaphore semaforoDeFluxo;
    private int baseDeEnvio;
    private int proxNumDaSequenciaDePacotes;
    private SortedMap<Integer,Boolean> listaDeACKdePacotes;

    private Semaphore semaforoDeTimeouts;
    private SortedMap<Integer,Timer> listaDeTimersCorrentes;
    private SortedMap<Integer,TimerTask> listaDeTimerTasksCorrentes;

    private Semaphore semaforoDeReenvios;
    private SortedMap<Integer, byte[]> pacotesEmTimeout;
    private SortedMap<Integer, Integer> iteracaoDoTimeout;
    
    private Semaphore semaforoDeCongestionamento;
    private int baseDaJanelaDeCongestionamento;
    private int janelaDeCongestionamento;
    private int limiteDePartidaLenta;
    private int baseDaJanelaAnteriorDeCongestionamento;
    private int janelaDePctEnviadosDeCongestionamento;

    private boolean aTransferenciaTerminou;

    private ThreadDeEntrada threadDeEntrada;
    private ThreadDeSaida threadDeSaida;

    private CriptografiaAES criptografia;

    private double inicioDeTransmissao;
    private double fimDeTransmissao;

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

        this.roteador = roteador;
        
        this.tamanhoDoPacote = 1000;
        this.tamanhoDeJanelaDeRepeticaoSeletiva = 10;
    
        this.atrasoDePropagacao = 0;
        this.atrasoDeTransmissao = 0;
        this.probabilidadeDePerda = 0;

        this.tempoDeTimeoutDePacote = 300;

        this.semaforoDeFluxo = new Semaphore( 1 );
        this.baseDeEnvio = 0;
        this.proxNumDaSequenciaDePacotes = 0;
        this.listaDeACKdePacotes = new TreeMap<Integer,Boolean>();
        for ( int i = baseDeEnvio; i < this.tamanhoDeJanelaDeRepeticaoSeletiva; i++ ) 
        {
            this.listaDeACKdePacotes.put( i, false );
        }

        this.semaforoDeTimeouts = new Semaphore( 1 );
        this.listaDeTimersCorrentes = new TreeMap<Integer,Timer>();
        this.listaDeTimerTasksCorrentes = new TreeMap<Integer,TimerTask>();

        this.semaforoDeReenvios = new Semaphore( 1 );
        this.pacotesEmTimeout = new TreeMap<Integer,byte[]>();
        this.iteracaoDoTimeout = new TreeMap<Integer,Integer>();

        this.semaforoDeCongestionamento = new Semaphore( 1 );
        this.baseDaJanelaDeCongestionamento = 0;
        this.janelaDeCongestionamento = 0;
        this.limiteDePartidaLenta = Integer.MAX_VALUE;
        this.baseDaJanelaAnteriorDeCongestionamento = -1;
        this.janelaDePctEnviadosDeCongestionamento = -1;

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

    public void setTamanhoDaJanelaDeRepeticaoSeletiva ( int tamanhoDeJanela )
    {
        this.tamanhoDeJanelaDeRepeticaoSeletiva = tamanhoDeJanela;

        this.listaDeACKdePacotes = new TreeMap<Integer,Boolean>();
        for ( int i = baseDeEnvio; i < this.tamanhoDeJanelaDeRepeticaoSeletiva; i++ ) 
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

    void setInicioDeTransmissao () 
    {
        this.inicioDeTransmissao = System.currentTimeMillis();
    }

    void setFimDeTransmissao ()
        throws Exception
    {
        this.fimDeTransmissao = System.currentTimeMillis();

        reportarTaxaMedia();
    }

    private void reportarTaxaMedia() 
        throws Exception
    {

        double tempoDeTransmissao = ( this.fimDeTransmissao - this.inicioDeTransmissao ) / 1000;
        double taxaMediaTransmissao = ( 
            this.mensagemDeEnvio.getBytes().length
            / tempoDeTransmissao );

        for ( int i = 1; i < 100; i++ ) {

            String path = 
                "resultados" 
                + File.separator 
                + "taxas_de_transmissoes" 
                + File.separator 
                + this.getDenominacao() 
                + "_"
                + i
                + ".txt";
            
            File f = new File( path );
            if( ! f.exists() && ! f.isDirectory() ) { 

                f.getParentFile().mkdirs();
                f.createNewFile();
        
                BufferedWriter writer = 
                new BufferedWriter( 
                    new FileWriter(
                        new File ( path )
                    ) 
                );
                writer.write( "TEMPO TOTAL (s): " + tempoDeTransmissao + "\n" );
                writer.write( "TAMANHO DA MENSAGEM (b): " + this.mensagemDeEnvio.getBytes().length + "\n" );
                writer.write( "TAXA MEDIA DE TRANSMISSAO (b/s): " + taxaMediaTransmissao + "\n" );
                writer.close();

                break;
            }
            
        }

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

    int getTamanhoDoPacote () 
    {
        return this.tamanhoDoPacote;
    }

    int getTamanhoDeJanelaDeRepeticaoSeletiva () 
    {
        return this.tamanhoDeJanelaDeRepeticaoSeletiva;
    }

    int getJanelaDeCongestionamento () 
    {
        return this.janelaDeCongestionamento;
    }

    int getBaseDaJanelaDeCongestionamento () 
    {
        return this.baseDaJanelaDeCongestionamento;
    }
    
    Semaphore getSemaforoDeFluxo () 
    {
        return this.semaforoDeFluxo;
    }

    int getBaseDeEnvio () 
    {
        return this.baseDeEnvio;
    }

    int getProxNumDaSequenciaDePacotes () 
    {
        return this.proxNumDaSequenciaDePacotes;
    }
    
    Semaphore getSemaforoDeTimeouts () 
    {
        return this.semaforoDeTimeouts;
    }
    
    Semaphore getSemaforoDeReenvios () 
    {
        return this.semaforoDeReenvios;
    }
    
    Semaphore getSemaforoDeCongestionamento () 
    {
        return this.semaforoDeCongestionamento;
    }

    boolean aTransferenciaTerminou () 
    {
        return this.aTransferenciaTerminou;
    }
    
    CriptografiaAES getCriptografia () 
    {
        return this.criptografia;
    }

    double getInicioDeTransmissao ()
    {
        return this.inicioDeTransmissao;
    }

    /**
     * 
     *      MÉTODOS IMPLEMENTADOS
     * 
     */

    boolean verificarACKdaJanelaDeCongestionamentoAnterior ()
        throws Exception
    {

        if ( this.baseDaJanelaAnteriorDeCongestionamento == -1 )
        {
            return true;
        }

        if (
            this.listaDeACKdePacotes.firstKey() > this.baseDaJanelaAnteriorDeCongestionamento
        )
        {
            return true;
        }
        else {
            
            for ( int numPacote : this.listaDeACKdePacotes.keySet() )
            {

                if ( 
                    numPacote > 
                    this.baseDaJanelaAnteriorDeCongestionamento + this.janelaDePctEnviadosDeCongestionamento 
                )
                {
                    return false;
                }
                else if (
                    ! this.existemPacotesEmTimeout()
                    && numPacote == this.baseDaJanelaAnteriorDeCongestionamento
                )
                {
                    return true;
                }
                else if (
                    numPacote >= this.baseDaJanelaAnteriorDeCongestionamento
                    && this.listaDeACKdePacotes.get( numPacote ) == true 
                )
                {
                    return true;
                }

            }

        }

        return false;

    }

    void configureBaseDaJanelaDeCongestionamento ( 
        int base,
        int janelaAtual
    ) 
    {
        this.baseDaJanelaAnteriorDeCongestionamento = base;
        this.janelaDePctEnviadosDeCongestionamento = janelaAtual;
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
        this.listaDeTimerTasksCorrentes.clear();

        this.listaDeTimersCorrentes.forEach( 
            (k, v) -> {
                if ( v != null )
                {
                    v.cancel();
                    v.purge();
                }
            }
        );
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

    SortedMap.Entry<Integer, byte[]> removerPacoteEmTimeout () 
    {
        int numPacote = this.pacotesEmTimeout.firstKey();
        byte[] pacote = this.pacotesEmTimeout.remove( numPacote );
        return new AbstractMap.SimpleEntry<Integer, byte[]>(
            numPacote, 
            pacote
        );
    }

    int removerIteracaoDeTimeout ( int numDoPacote ) 
    {
        return this.iteracaoDoTimeout.remove( numDoPacote );
    }
    
    boolean existemPacotesEmTimeout () {
        return this.pacotesEmTimeout.size() > 0;
    }

    void adicionarPacoteEmTimeout( int numPacote, byte[] pacote, int iteracao ) 
    {
        this.pacotesEmTimeout.put( numPacote, pacote );
        this.iteracaoDoTimeout.put( numPacote, iteracao );
    }

    void adicionarTimeout ( int numDoPacote, byte[] pacote, int iteracao )
        throws Exception
    {

        Timer timer = new Timer();
        TimerTask task = new TimeoutTask( this, numDoPacote, pacote, iteracao );
        int timeout = this.tempoDeTimeoutDePacote * iteracao;
        timer.schedule( task, timeout );

        this.listaDeTimerTasksCorrentes.put( numDoPacote, task );
        this.listaDeTimersCorrentes.put( numDoPacote, timer );

    }

    void removerTimeoutTask ( int numPacote )
        throws Exception
    {

        if ( this.listaDeTimersCorrentes.containsKey( numPacote ) )
        {

            if ( this.listaDeTimerTasksCorrentes.containsKey( numPacote ) )
            {
                TimerTask task = this.listaDeTimerTasksCorrentes.get( numPacote );
                task.cancel();
            }
            this.listaDeTimerTasksCorrentes.remove( numPacote );

            Timer timer = this.listaDeTimersCorrentes.get( numPacote );
            timer.cancel();
            timer.purge();

            this.listaDeTimersCorrentes.remove( numPacote );

        }

    }

    void atualizarJanelaDeRepeticaoSeletiva ( int numDeACK ) 
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
                        this.baseDeEnvio + this.tamanhoDeJanelaDeRepeticaoSeletiva,
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

    void adicionarACKaReceber( int numDoPacote ) {
        this.listaDeACKdePacotes.put( numDoPacote, false );
    }

    void sinalizarTerminoDaTransferencia () 
    {
        this.aTransferenciaTerminou = true;
    }

    void incrementeProxNumDaSequenciaDePacotes () 
        throws Exception
    {
        this.proxNumDaSequenciaDePacotes++;
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
