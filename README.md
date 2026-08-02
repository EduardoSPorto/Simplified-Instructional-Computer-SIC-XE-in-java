# Implementação do SIC/XE
Trabalho desenvolvido para a disciplina Programação de Sistemas no semestre 2024/2 contendo os seguintes membros:
* Eduardo Porto (**Lider**)
* Enthony Bohm (**Vice-Lider**)
* Diego Aquino
* Hendrick Bahr
* Laiane Souza
* Lucas Ludwig
* Sandro Frizon

# Executando os Testes
Nós Separamos uma pasta com dois testes Simples que demonstram o funcionamento da nossa máquina Virtual.
Para executa-los basta copiar o conteúdo das pastas, 2Módulos ou ExpansãoMacros, localizados dentro do diretório Casos de Teste, e colocar os .asm em Entradas Macro.

### Expansão de Macros

No Teste Expansão Macros, podemos verificar o funcionamento do Processador de Macros e sua capacidade de reutilizar trechos de código já definido, substituindo pela sua chamada no código. É um teste relativamente Simples com o único intuito de comprovar o funcionamento e eficácia do nosso processador de macros. 
* Os resultados esperados encontram-se no começo da memória, e são 0,5,5,10.
* Para verificar, deve-se considerar que a memória apesar de apresentar palavras de 3 bytes, devido a diversidade de instruções e tamanhos, está sendo lida byte a byte, para facilitar a leituras, as variáveis importantes foram colocadas no começo da memória.

### 2 Módulos
Para o teste de 2 Módulos, buscamos representar a capacidade do nosso Simulador SIC/XE em tratar dois diferentes módulos, ajustando a sua posição, relativa a onde iniciará cada módulo assim como, buscamos demonstrar a funcionalidade do endereçamento a um módulo externo, e como ele é ajustado no Loader. 
Além disso, foi adicionado uma instrução do tipo 2, que em teoria não apresenta mudança significativa ao código, mas ela serve para demonstrar como o endereçamento não necessariamente precisa ser de 3 em 3 bytes, adaptando-se a instruções de tamanhos diferentes.
O teste consiste na definição de dois valores, que irão ser somados e em seguida multiplicados no primeiro segmento, enquanto no segundo segmento esses valores serão recuperados para realizar uma subtração do valor multiplicado pelo valor somado. Vale ressaltar que nosso simulador opera somente com inteiros positivos.
* Primeiras palavras da memória contém os valores 2 e 3, na terceira e quarta palavra, respectivamente, esperava os valores 5 e 6. Na última posição reservada de memória, espera-se o valor da operação 6-5, ou seja, o último valor resultante do caso de teste é 1.
    

# Considerações Finais
Nosso código não pode ser considerado completamente como uma implementação do Simulador SIC/XE mas sim um simulador de máquina virtual inspirado em SIC/XE por apresentar algumas características que não estão presentes no SIC/XE original
* Text do Object Program com extrutura inspirada no modelo Calingaert
* Adição de um byte de tipo, no Text, definindo como instrução ou diretiva, para gerar um Map de posições de instruções para a Máquina atualizar o Program Counter.

Nosso código não teve foco em garantir que entradas com muito espaços possuam um "trim" para manter somente a instrução, portanto, em alguns casos, isso pode resultar em exceções.
Espera-se que a sintaxe utilizada seja em maísculo, podendo ter tabulações somente na definição de uma macro. Isso consta para a versão atual (V 1.0 Entrega)


Agradeço pela sua atenção
