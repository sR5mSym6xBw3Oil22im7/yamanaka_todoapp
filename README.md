# Todoアプリ

Spring Bootで作成した、タスク（Todo）の登録・管理アプリケーションです。タスクの検索、登録、編集、完了管理、削除、期限の確認を行えます。

## 主な機能

- Todoの一覧表示とページング（1ページ10件）
- タイトルのキーワード検索、カテゴリによる絞り込み
- 期限の近い順・遠い順での並べ替え
- Todoの登録・編集・完了状態の変更・削除
- ピン留め、削除済みTodoの確認と復元
- 月表示・週表示に対応したカレンダー
- 祝日・天気情報の取得
- REST API、CSVエクスポート
- Model Context Protocol（MCP）によるTodo操作

## 使用技術

- Java 21
- Spring Boot 4.0.8
- Spring MVC / Thymeleaf
- MyBatis
- MySQL 8.0
- Spring AI MCP Server
- Maven

## 必要な環境

- Java 21以上（ローカル実行時）
- Docker / Docker Compose（コンテナ実行時）

## 起動方法

### Docker Composeを使用する場合

プロジェクトのルートで、MySQLのrootパスワードを設定して起動します。

```powershell
$env:MYSQL_ROOT_PASSWORD = "MySQLのrootパスワード"
docker compose up --build
```

起動後、<http://localhost:8080/> にアクセスしてください。

初回起動時は、`initdb/` 配下のSQLファイルでデータベースとサンプルデータが初期化されます。

### ローカルで実行する場合

MySQLに `todoapp` データベースを用意し、接続情報を設定してください。既定値は次のとおりです。

| 項目 | 値 |
|---|---|
| ホスト | `localhost:3306` |
| データベース | `todoapp` |
| ユーザー | `root` |
| パスワード | `password` |

次のコマンドで起動します。

```powershell
./mvnw spring-boot:run
```

Windowsでは `./mvnw.cmd spring-boot:run` も使用できます。

## 画面とURL

| 画面 | URL | 内容 |
|---|---|---|
| トップページ | `/` | アプリのトップページ |
| Todo一覧 | `/todos` | 検索、並べ替え、一覧表示 |
| Todo登録 | `/todos/new` | Todoの新規登録 |
| Todo編集 | `/todos/{id}/edit` | Todoの編集 |
| 削除確認 | `/todos/{id}/delete` | Todoの削除 |
| カレンダー | `/calendar` | 期限の月・週表示 |

一覧画面では、`keyword`、`category`、`order`（`asc` / `desc`）、`page`、`trash`（`1`で削除済み表示）を指定できます。

例：

```text
/todos?keyword=資料&category=プログラミング&order=asc&page=1
```

## カテゴリ

- デザイン
- マーケティング
- プログラミング
- 資格
- 趣味・生活

## REST API

| メソッド | エンドポイント | 内容 |
|---|---|---|
| `GET` | `/api/todos` | Todo一覧を取得 |
| `GET` | `/api/todos/{id}` | Todoを1件取得 |
| `POST` | `/api/todos` | Todoを作成 |
| `PUT` | `/api/todos/{id}` | Todoを更新 |
| `DELETE` | `/api/todos/{id}` | Todoを削除 |
| `GET` | `/api/todos.csv` | TodoをCSVで取得 |
| `GET` | `/api/holidays` | 祝日情報を取得 |
| `GET` | `/api/weather` | 天気情報を取得 |

`/api/todos` では、`keyword`、`category`、`order`、`from`、`to`（日付形式：`yyyy-MM-dd`）を指定できます。

## MCP

MCPサーバーを有効にしており、MCPクライアントからTodoの一覧取得、追加、更新、完了、削除、週次集計、空き日の検索を実行できます。通信プロトコルはStreamable HTTPです。

## テスト

```powershell
./mvnw test
```

画面操作の確認項目は [test-spec.html](test-spec.html) にまとめています。

## ディレクトリ構成

```text
src/main/java/                 Javaソースコード
src/main/resources/templates/  Thymeleafテンプレート
src/main/resources/static/     CSSなどの静的ファイル
src/main/resources/mapper/     MyBatisマッパー
initdb/                        データベース初期化SQL
docker-compose.yml             アプリとMySQLの定義
```
