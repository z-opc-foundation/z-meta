/** ./pages 入口：manifest + apiBase 注入（§8.2/§8.3/§9.3）。 */

// 整份转发，不要手挑名字：原先写死 menuItems/routes/Dashboard/Login，
// 而 manifest 还导出 HomePage / LoginPage —— suit 的 App.jsx 正好 import 这两个，
// 于是 rollup 报 "LoginPage" is not exported by dist/pages.js。再挑一次名字还会重演。
export * from './pages-manifest.jsx';
export {configureMeta} from './services/request.js'
