const env = process.env
const express = require('express');
const { Client } = require("@notionhq/client");
const { NotionToMarkdown } = require("notion-to-md");

const app = express();
const port = 3000;

const notion = new Client({ auth: env.NOTION_API_KEY });
const n2m = new NotionToMarkdown({ notionClient: notion });
// ファイル名を設定
const filename = "exported.md";

app.get('/notionToMd', async (req, res) => {
    const pageId = req.query.pageId;
    const mdblocks = await n2m.pageToMarkdown(pageId);
    const mdString = n2m.toMarkdownString(mdblocks);

    // レスポンスヘッダーを設定
    res.setHeader('Content-disposition', 'attachment; filename=' + filename);
    res.setHeader('Content-type', 'text/markdown');

    // Markdownデータを送信
    res.send(mdString.parent);
});

app.get('/healthCheck', (req, res) => {
    res.send();
});

// サーバーをポート3000で起動
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
