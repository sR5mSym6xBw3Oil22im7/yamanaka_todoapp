# DB設計書

## 1. 文書の目的

この文書は、やること管理アプリが使うデータベース（データを保存する仕組み）の設計を定めるものです。

保存先は MySQL（データベース製品）の `todos` テーブル（表）1つだけとします。1件のやることを、`todos` の1行に保存します。

## 2. 設計方針

- テーブルは `todos` の1つだけにする。
- 利用者は1人で、利用者を識別する `user_id` は持たない。
- `id` は自動的に増える主キー（1件を一意に特定する番号）とする。
- 必須項目は `NOT NULL`（空の値を許可しない）にする。
- 期限とメモは任意項目なので、入力がない場合は `NULL`（値がない状態）を許可する。
- `category`（ジャンル）と `priority`（優先度）は、決められた値だけを保存できるようにする。
- 登録日時と更新日時は、MySQLが自動的に設定・更新する。

## 3. テーブル一覧

| テーブル名 | 用途 | 行（データ1件）の意味 |
|---|---|---|
| `todos` | やることの保存 | 1件のやること |

## 4. `todos` テーブル定義

### 4.1 カラム一覧

カラム（列）は、1件のやることを構成する項目です。

| カラム名 | データ型 | NULL | 初期値 | キー・制約 | 内容 |
|---|---|---|---|---|---|
| `id` | `BIGINT` | 不可 | 自動採番 | `PRIMARY KEY`, `AUTO_INCREMENT` | やることを特定する番号 |
| `title` | `VARCHAR(255)` | 不可 | なし | 255文字以内 | やることの名前 |
| `detail` | `VARCHAR(255)` | 可 | `NULL` | 255文字以内 | メモ |
| `category` | `VARCHAR(255)` | 不可 | なし | 指定された5種類のいずれか | ジャンル |
| `priority` | `INT` | 不可 | `2` | `1`・`2`・`3`のみ | 1=高、2=中、3=低の優先度 |
| `due_date` | `DATE` | 可 | `NULL` | なし | 期限。日付だけを保存 |
| `completed` | `BOOLEAN` | 不可 | `FALSE` | `TRUE`または`FALSE` | 完了状態。FALSE=未完了、TRUE=完了 |
| `created_at` | `DATETIME` | 不可 | 現在日時 | `DEFAULT CURRENT_TIMESTAMP` | 登録日時 |
| `updated_at` | `DATETIME` | 不可 | 現在日時 | `ON UPDATE CURRENT_TIMESTAMP` | 最終更新日時 |

### 4.2 `category` の保存値

`category` は自由入力ではなく、次の5種類の文字列のどれかを保存します。

- `デザイン`
- `マーケティング`
- `プログラミング`
- `資格`
- `就職活動`

### 4.3 `priority` と `completed` の保存値

| カラム | 保存値 | 画面で表示する言葉 |
|---|---|---|
| `priority` | `1` | 高 |
| `priority` | `2` | 中 |
| `priority` | `3` | 低 |
| `completed` | `FALSE`（MySQLでは0として扱われる） | 未完了 |
| `completed` | `TRUE`（MySQLでは1として扱われる） | 完了 |

## 5. CREATE TABLE文（DDL）

DDL（Data Definition Language、テーブルを作る命令）は次のとおりです。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category
        CHECK (category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')),
    CONSTRAINT chk_todos_priority
        CHECK (priority IN (1, 2, 3)),
    INDEX idx_todos_category (category),
    INDEX idx_todos_due_date (due_date)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
```

`InnoDB` はMySQLの保存方式、`utf8mb4` は日本語などを保存する文字コード（文字の保存方法）です。`category` と `due_date` のインデックス（検索・並び替えを速くするための目印）を作ります。

## 6. 要件とカラムの対応

### 6.1 保存項目・入力項目との対応

| 要件 | 対応するカラム | DBでの対応 |
|---|---|---|
| やることを保存する | `title` | `VARCHAR(255) NOT NULL` |
| メモを保存する | `detail` | `VARCHAR(255) NULL` |
| 5種類のジャンルから選ぶ | `category` | `NOT NULL` と `CHECK` で5種類に限定 |
| 優先度を保存する | `priority` | `INT NOT NULL DEFAULT 2` と `CHECK` で1〜3に限定 |
| 期限を任意で保存する | `due_date` | `DATE NULL` |
| 完了・未完了を保存する | `completed` | `BOOLEAN NOT NULL DEFAULT FALSE` |
| 1件を特定して編集・削除する | `id` | 自動採番の主キー |
| 登録日時を記録する | `created_at` | MySQLが登録時に自動設定 |
| 最終更新日時を記録する | `updated_at` | MySQLが登録時に設定し、更新時に自動更新 |

### 6.2 機能との対応

| 機能・要件 | 使用するカラム | 保存・取得方法 |
|---|---|---|
| 一覧表示 | `title`, `category`, `priority`, `due_date`, `completed`, `id` | `todos` から取得し、優先度と完了状態は画面用の言葉に変換 |
| 登録 | `title`, `detail`, `category`, `priority`, `due_date` | 入力チェック後に1行追加。`id`・日時3項目は自動設定 |
| 編集 | `id` と登録項目、`completed` | `id` で対象行を特定して更新 |
| 削除 | `id` | `id` で対象行を特定して削除 |
| 名前による絞り込み | `title` | 入力された文字列を一部に含む行を検索（部分一致） |
| ジャンルによる絞り込み | `category` | 選択された値と完全一致する行を検索 |
| 期限による並び替え | `due_date` | 近い順は昇順（小さい日付から）、遠い順は降順（大きい日付から） |
| 存在しない番号への対応 | `id` | 指定した `id` の行がない場合は画面を表示せず一覧へ戻す |

検索・並び替えの条件は画面からURL（ページの住所）の後ろに渡されます。DBでは、受け取った条件を `title`・`category`・`due_date` に対応させます。

## 7. 入力チェックとDB制約の分担

入力チェック（画面で入力内容を確認する処理）は、利用者に分かりやすいエラー文を表示するためにアプリ側で行います。DB側の制約は、アプリ側のチェックを通らないデータが保存されることも防ぎます。

| チェック内容 | 主に担当する場所 | 対応 |
|---|---|---|
| `title` が空、空白だけ | アプリ側 | 保存前に空として扱う |
| `title` が255文字超過 | アプリ側・DB | 入力画面でエラー表示、DB型でも上限を定義 |
| `detail` が255文字超過 | アプリ側・DB | 入力画面でエラー表示、DB型でも上限を定義 |
| `category` が未選択 | アプリ側・DB | 入力画面でエラー表示、`NOT NULL` と `CHECK` で防止 |
| `priority` が未選択、または範囲外 | アプリ側・DB | 入力画面でエラー表示、`NOT NULL` と `CHECK` で防止 |
| `completed` の初期状態 | DB | `DEFAULT FALSE` |

期限についてはアプリ側の入力チェックを追加しません。ただし、日付として不正な値は `DATE` 型（年月日を保存する型）の制限により保存できません。

## 8. ログ（操作記録）の扱い

要件では、登録・編集・削除が成功したときに、操作の種類と対象の `id` を1操作につき1行記録します。また、`title` と `detail` の中身は記録しません。

ただし、このアプリで使うDBテーブルは `todos` の1つだけという前提なので、ログ用テーブルやログ用カラムは追加しません。操作記録はアプリケーションのログファイル（アプリが出力する記録ファイル）に、1操作1行で次の情報だけを出力します。

| 記録する情報 | 内容 |
|---|---|
| 操作種別 | 登録・編集・削除 |
| 対象 | `todos.id` |
| 記録しない情報 | `title`、`detail` の内容 |

## 9. 保存しないもの

次の情報は要件の対象外なので、`todos` にはカラムを作りません。

- 利用者・ログイン情報
- 複数利用者を区別する情報
- 完了にした日時
- 削除済みデータを戻すための情報
- 写真やファイルの情報
- 一覧のページ番号

## 10. 前提・注意事項

- `id`、`created_at`、`updated_at` は利用者の入力欄に表示しません。
- `updated_at` は、`todos` の行を更新したときにMySQLが自動更新します。完了状態の変更も更新に含まれます。
- `CHECK` 制約（保存できる値を限定する仕組み）を確実に利用するため、MySQL 8.0.16以降を前提とします。
- 期限が未入力の行は `due_date` が `NULL` です。期限順の一覧では、未入力の期限を画面上でどう扱うかを、画面設計書で定めます。DB設計では期限を任意として保存できることを定義します。
