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


## Continuação da auditoria e remediação — 15/10/2026

Commits adicionais:
- b30cad90: cópias privadas de entradas corrompidas do índice de planilhas;
- c59b1c10: Dependabot semanal e revisão de dependências em PR;
- 4cf9db5b: artefatos debug separados e marcados TEST-ONLY;
- cb5b937a: salvamento no ciclo de vida ao ir para segundo plano;
- e370cc69: testes unitários de regressão em Pull Requests;
- afb22fdf: validação estrutural prévia da importação pública de NPC;
- 68b4f501: testes de recuperação/importação obrigatórios nos workflows;
- a8e78650: workflow CodeQL Kotlin de execução manual;
- a0ea26cc: restauração do contrato textual do PDF sem perder aviso de privacidade.

### Primeira execução real de testes de Pull Request

Duas execuções de PR Dependabot produziram 1.160 testes executados,
com 1 falha de contrato textual: EncounterPdfExportContractTest exigia
a frase 'PDF salvo em Gerador Harmônico/NPC\'s:'.
O código havia mudado a mensagem para indicar que a pasta era pública;
o commit a0ea26cc restaurou a frase exigida e manteve indicação explícita
de armazenamento público. Essa correção ainda NÃO foi revalidada numa
nova execução de testes da branch principal.

### Pré-requisito externo bloqueando Dependency Review

A execução do workflow 'Dependency security review' informou:
'Dependency review is not supported on this repository. Please ensure
that Dependency graph is enabled'.

Ação necessária pelo administrador:
https://github.com/rpgexalted001/Planilha-Otimizada/settings/security_analysis
Habilitar Dependency graph e, se disponível, Dependabot alerts e security updates.
O repositório é público; o problema é configuração externa à árvore de código.

Dependabot abriu PRs de atualização de AGP, Kotlin, Compose BOM, Gradle e
GitHub Actions. **Não fundir automaticamente**, pois é necessária compatibilidade
com AGP 9.x, Gradle 9.8, testes mecânicos dos três tipos de Exaltado e
validação de atualização do APK assinado.

### Limitações ainda existentes

- O APK Exalted.961 anterior não contém estes commits.
- A correção do contrato do PDF ainda precisa ser executada em CI.
- Auditoria CodeQL criada, mas não executada; ela requer compilação Kotlin
  para produzir uma base de análise útil, embora não gere APK.
- Testes de UI instrumentados, backup/restore após reinstalação em aparelhos
  e continuidade do certificado de assinatura ainda requerem validação.
- Salvamento final síncrono no onCleared ainda deve ser medido sob I/O lento,
  porque pode prejudicar a responsividade; o flush ao ir para background
  foi acrescentado como mitigação.
- Sem ativar Dependency graph, o novo check não pode inspecionar
  vulnerabilidades de dependências.
- Mudanças de fonte/documentação usam [skip ci] e não alteraram
  settings.gradle.kts; não foi autorizada nova compilação de APK.
