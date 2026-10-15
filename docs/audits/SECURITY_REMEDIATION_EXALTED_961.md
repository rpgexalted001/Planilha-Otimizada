# Exalted.961 — correções após auditoria estática

Estado: **alterações registradas no código, NÃO compiladas em um novo APK**.
Repositório oficial: rpgexalted001/Planilha-Otimizada, branch main.
A versão canônica permanece Exalted.961 até uma compilação futura expressamente autorizada.

## Implementado (sem alterar regras do jogo ou identidade visual)

1. SheetPersistenceCoordinator: preserva o último snapshot pendente/em voo no encerramento,
   aguarda cancelamento do worker antes de gravar o mais recente e não aceita submits após close.
   SheetViewModel.onCleared() enfileira explicitamente o estado mais recente da planilha e iniciativa
   antes de fechar o repositório. Incluídos testes de encerramento/corrida.
2. EncounterNpcStore / NpcStore: falha ao desserializar um NPC não apaga os demais NPCs
   válidos. Uma cópia privada do índice originalmente corrompido fica preservada em SharedPreferences.
   Teste Robolectric adicionado para isolamento de corrupção da lista de Encontros.
3. Recuperação de documentos: SAF/OpenDocument permite importar JSON de Jogador e .save
   de NPC até 4 MiB, sem permissões amplas de armazenamento. IDs são regenerados no destino
   para evitar colisões. O arquivo JSON importado vira uma planilha local independente.
   Testes do limite de leitura de arquivo adicionados.
4. Exportação pública: falhas ao publicar JSON/código em Documents são comunicadas ao
   usuário por Snackbar. Gravação privada permanece distinta da cópia pública.
5. Workflow Build APK: compilações normais exigem os quatro GitHub Secrets de assinatura
   persistente; opção manual explícita allow_debug_signing libera somente um APK de TESTE,
   quando todos os secrets estão ausentes. Secret parcial é sempre erro.
   Verifica o keystore via keytool e rejeita certificado Android Debug no caminho oficial.
   Contrato estático de segurança adicionado.

Commits: 4f098494, cbc228f, 0fb0dc4, 852cb0c.

## Pendências importantes

- Configurar SIGNING_KEY (keystore em Base64), KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
  no novo repositório. **Não** copiar valores de senhas para issues/chats/logs.
- Confirmar continuidade do certificado da versão já instalada antes de atualizar o APK:
  versões debug diferentes ou assinatura oficial não são atualizações compatíveis.
- Compilar próximo número sequencial mediante autorização e executar testDebugUnitTest,
  engenharia/regressão, inspeção de APK assinado, teste no aparelho e teste Android SAF.
- Testes instrumentados de interface (app/src/androidTest), scanners de dependências e
  análise dinâmica da segurança continuam pendentes.
- Backups periódicos ainda descartam snapshots pendentes no encerramento; prioridade
  menor que a planilha ativa, mas requer teste e correção específica.
- Exportações PDF/JSON em Documents são legíveis fora da sandbox por decisão do produto:
  não tratá-las como armazenamento privado ou criptografado.
- Verificar Android 8/9 com permissão legada de gravação em Documents e Android 10+ via SAF.
- O fechamento síncrono de persistência deve ser medido em dispositivos lentos para
  verificar se o flush pode afetar latência da interface.
- Ampliar testes de importação de JSON estruturalmente inválido e compatibilidade
  com versões antigas; o limite de 4 MiB é apenas uma das proteções.
- Investigar as dependências reais em busca de CVEs e realizar uma matriz de regressão
  com os três tipos de Exaltado e as 15 abas.

## Critério de liberação

Não declarar a correção comprovada em execução antes da compilação e de testes em
Android reais ou emuladores. O sucesso anterior de Exalted.961 é do código ANTERIOR
a estes commits, não deste pacote.
