### 需求

<table>
  <tr>
  	<td>场景</td>
    <td>需求点</td>
    <td>是否完成</td>
  </tr>
	<tr>
		<td rowspan="5" style="vertical-align:middle">算法-用户画像</td>
		<td>能够记录用户的行为数据，结合标签信息等信息内容特征、用户特征，通过采用大数据算法和用户行为数据实现特定用户精准画像。</td>
    <td><input type="checkbox"/></td>
	</tr>
  <tr>
		<td>能够依据用户群体的行为数据，结合标签信息等信息内容特征、用户特征，对用户群体进行聚类和画像。</td>
    <td><input type="checkbox"/></td>
	</tr>
  <tr>
		<td>能够依据全部用户的行为数据，结合标签信息等信息内容特征、用户特征，对全部用户进行整体画像。</td>
    <td><input type="checkbox"/></td>
	</tr>
  <tr>
		<td>并提供工具，通过搜索用户、关注用户的方式，查看用户画像和行为数据。</td>
    <td><input type="checkbox"/></td>
	</tr>
  <tr>
		<td>根据建立的用户画像，部署本地化推荐服务，实现最新信息精准推荐，为用户提供满足其需要和兴趣的特定内容，提升用户粘度。</td>
    <td><input type="checkbox"/></td>
	</tr>
</table>


### 数据源

​	**现在只做头条、百科的内容。**

<table>
  <tr>
    <td>服务器</td>
    <td colspan="3">231</td>
  </tr>
  <tr>
    <td>端口</td>
    <td colspan="3">3307</td>
  </tr>  
  <tr>
    <td>库名</td>
    <td colspan="3">zbzs</td>
  </tr>  
  <tr>
    <td rowspan="7">表名</td>
    <td>user_operate_info</td>
    <td>用户操作日志表</td>
    <td>paramid 对应 toutiao_info_ref 的 id / wiki_info_ref 的 auto_id</td>
  </tr>  
  <tr>
    <td>user_read_record</td>
    <td>用户阅读记录</td>
    <td>info_type=1 是头条，ref_data_id 是 toutiao_info_ref 的 id ；<br>info_type=2 是百科，ref_data_id 是 wiki_info 的 auto_id </td>
  </tr>
  <tr>
  	<td>data_up_info</td>
    <td>用户点赞表</td>
    <td>info_type=1 是头条，ref_data_id 是 toutiao_info_ref 的 id ；<br>info_type=2 是百科，ref_data_id 是 wiki_info 的 auto_id </td>
  </tr>
  <tr>
  	<td>data_collection</td>
    <td>用户收藏表</td>
    <td>info_type=1 是头条，ref_data_id 是 toutiao_info_ref 的 id ；<br>info_type=2 是百科，ref_data_id 是 wiki_info 的 auto_id </td>    
  </tr>
  <tr>
    <td>recommended_behave</td>
    <td>推荐行为表：用户查询、订阅信息</td>
    <td>info_type=1 是头条，ref_data_id 是 toutiao_info_ref 的 id ；<br>info_type=2 是百科，ref_data_id 是 wiki_info_ref 的 auto_id </td>    
  </tr>  
  <tr>
    <td>user_search_record</td>
    <td colspan="2">用户查询有结果的</td>
  </tr>
  <tr>
    <td>user_search_history</td>
    <td colspan="2">用户查询无结果的</td>
  </tr>
</table>


**表映射：**

| 原始表          | 智搜库表                  | 字段映射                                                     |
| --------------- | ------------------------- | ------------------------------------------------------------ |
| users           | app_user                  | id - id<br />pref_list - pref_list（新添字段）               |
| item            | toutiao_info_ref          | id - id<br />content - infoDesc<br />news_time - publishTime<br />title - infoTitle<br />module_id - classifySubName |
| newslogs        | user_read_record          | id - id<br />user_id - user_id<br />news_id - ref_data_id（info_type=1头条，2百科）<br />view_time - insert_time |
| newsmodules     | info_ref_calssify_type    | id - id<br />name - classifName（classifyParentId不为-1的）  |
| recommendations | recommendations（新添表） | id - id<br />user_id - user_id<br />news_id - item_id<br />derive_time - derive_time <br />derive_algorithm - derive_algorithm |




### 技术选型调研

#### 常用推荐算法优劣

- **基于用户的协同过滤**

  - 思想

    基于用户对物品的偏好，找到相似用户，再将相似用户的偏好的东西推荐给当前用户。

  - 计算

    将一个用户对所有物品的偏好作为一个向量，计算**用户之间的相似度**。找到相似用户后，根据相似用户的权重及他们的物品偏好，预测当前用户未涉及的物品，计算得到一个排序的物品列表作为推荐。

  - 缺陷

    1. 大多数时候，用户两两之间只有很少几个共同评分，即用户之间重合度不高，且仅有的共同物品，往往是一些常见的物品。
    2. 用户之间的距离变化可能很快，这种离线算法难以瞬间更新推荐结果。

  - 场景

    适用于物品比用户多、物品时效性较长的场景。

    推荐结果个性化较弱、较宽泛。

- **基于物品的协同过滤**

  - 思想

    基于所有用户对物品的偏好找到相似的物品，然后根据当前用户的历史偏好，推荐相似的物品。

  - 计算

    将所有用户对某个物品的偏好作为一个向量来计算**物品之间的相似度**。得到物品的相似物品后，根据用户的历史偏好来预测当前用户未涉及的物品的偏好，计算得到一个排序的物品列表作为推荐。

  - 优点

    1. 物品之间的距离较为稳定。
    2. 预先计算距离，能够在线更快地生成推荐列表。

  - 缺点

    1. 不同领域的最热门物品之间经常具有较高的相似度。

  - 场景

    应用最为广泛，尤其以电商行业。

    适用于用户比物品多的情况。

- **基于模型的协同过滤**

  - 思想

    面对稀疏数据，先用历史数据得到一个模型，来用此模型进行预测。

    通过机器学习方法来建模，主流方法：关联算法、聚类算法、分类算法、回归算法、矩阵分解、神经网络、图模型以及隐语义模型。

- **基于内容的推荐**

  - 思想

    根据物品或内容的元数据，发现物品或内容的相关性，然后基于用户以往的偏好记录，给当前用户推荐相似的物品。

  - 缺陷

    1. 用户浏览的物品本身就不是用户的菜，再基于内容进行推荐就是伪命题。
    2. 当前信息已经解决用户的问题，再推类似主题的会造成信息冗余。

  - 场景

    最直观的算法，常借助文本相似度计算。

- **基于用户画像（标签）的推荐**

  - 思想

    依赖用户累积的行为数据，通过行为数据生成用户的兴趣标签，然后利用用户的画像属性来推荐。（注：兴趣会随时间迁移而改变）

  

#### 主流实现流程

召回 + 排序的流程方式

1. 为所有的新闻打上分类和关键词标签，主要离线进行，TF-IDF、LDA和聚类方式都可，准确率可以单独评估，关键是为文章打上标签，而且是带权的标签序列，文章的属性越丰富越好，时间、地域、长短、标题关键词、图片摘要等。另外对各类别简历文章的倒排索引。
2. 文章有标签后，可以离线或实时计算用户对类别或关键词的统计偏好，生成类别的用户画像，对单个用户来说也是带权的列表，可以存在redis中。
3. **召回阶段**。用户请求时拿到用户画像偏好，对标签列表中各标签分别从倒排索引中捞出文章数据，数量根据画像的权重分配，这样就拿到用户基本感兴趣的内容了。
4. **排序阶段**。基本的LR就可以。通过用户点击日志及离线计算的特征生成样本，训练模型，线上使用模型进行排序预测，生成最终的排序结果。

---

### 实际开发

#### 技术选型

使用混合推荐：

1. **基于协同过滤推荐（UserBased）**

   ```mermaid
   graph LR
   A[用户历史浏览记录] --> B[计算所有用户之间的相似度]
   B --> C[针对每个用户user]
   C --> D
   D[获取用户user的相似用户排序表] --> E[选择与user最相近的K个用户]
   E --> F[获取这K个用户浏览过,但user未浏览过的新闻]
   F --> C
   ```

   ​	**Mahout**：基于Java的数据挖掘与机器学习类库，提供推荐系统所需的分类、用户相似度计算，近邻用户计算等工具类。

2. **基于内容的推荐**

   ```mermaid
   graph LR
   A[用户历史浏览记录] --> B[计算新闻之间在内容上的相似度]
   B --> C[针对每个用户user]
   C --> D[获取与已浏览新闻相似的新闻]
   D --> C
   ```

   - **定义内容相似的方式**

     从文本特征几个方面提取特征信息，进而对不同新闻间的特征信息进行比较。

     常见的特征信息有：新闻文本长度、新闻所属话题类型、来源和关键词。

   - **提取新闻关键词的方式**

     TF-IDF 算法：统计方法，用以估计一字词对于一个文本集或一个语料库中的其中一份文件的重要程度。字词的重要性随着它再文本中出现的次数成正比增加，但同时会随着它在语料库中的出现频率成反比下降。

   - **用户偏好构建方式**

     关键词  -- 从用户历史浏览记录中挖掘。

     1. 在库中为每个用户维护一个关键词列表。
     2. 用户浏览了某个模块的某个新闻，利用TF-IDF算法提取出新闻的K个关键词，以及对应的TF-IDF值（关键程度），并将这些存入到对应的关键词列表中。
     3. 如果用户中已有某关键词及对应TF-IDF值，则将TF-IDF值叠加，表示加强用户对该关键词的感兴趣程度。
     4. 为关键词列表设置一个衰减系数 **λ**，每天对用户的偏好关键词的TF-IDF值进行更新，减少关键词的收敛倾向。

   - **新闻内容与用户偏好拟合度计算方式**

     基于用户的偏好关键词列表和某条新闻的关键词列表，对两个Map的键匹配与值的运算即可。

     若有相同的键，则值相乘，多个相同键的值乘积累加，若无相同的值，值记为0。

     将拟合度最高的N个新闻推送给用户。

     > 比如：
     >
     > 小黑在“武器装备”头条模块的关键词列表为 {坦克：100， 枪械：200...}
     >
     > “武器装备”头条模块中某个新闻的关键词列表为 {枪械：100，飞机：50}
     >
     > 则小黑与该新闻的拟合度为 200 x 100 = 20000。

     

3. **基于热点头条的推荐**

   ​	即`从用户浏览历史中，提取出近期被用户阅读最多的新闻`。

   ​	设定一个为用户推荐的最小新闻数量 N，若通过协同过滤和内容推荐的结果小于 N，则用热点头条方式作为补充，推荐给用户。

4. **随机补充推荐**

   无用户浏览记录的冷冷启动情况下，向用户推荐各领域下最新的五条新闻。

   

#### 开发进度

##### 2020.08.17
    - 配置Log4j、Mybatis，初步搭建项目。

##### 2020.08.18 
    - 实现获取时效内的热点新闻。
    - 部分实现基于内容的推荐系统。

##### 2020.08.19
    - 实现基于内容的推荐。
    - 实现对特定用户执行一次推荐。
    - 添加参数配置文件。
    - 实现基于用户的协同过滤推荐。

##### 2020.08.20
    - 实现用户偏好衰减更新。
    - 部分实现根据用户浏览历史，对用户偏好关键词及TF-IDF值更新。

##### 2020.08.21
    - 实现用户偏好根据浏览记录更新。
    - 实现基于内容的推荐。

##### 2020.08.24
    - 测试优化三种算法。
    - 实现随机补充推荐。

##### 2020.08.25
    - 实现与头条业务数据库结合。
    
##### 2020.08.26
    - 引入百科业务。

##### 2020.08.27 
    - 实现百科随机推荐。
    - 实现百科热点推荐。
    - 实现百科基于用户协同过滤推荐。
   

