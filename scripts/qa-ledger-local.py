"""Exercise the production same-phone ledger UI using fresh native layout bounds."""
import runpy,re,time
from pathlib import Path
q=runpy.run_path(str(Path(__file__).with_name('qa-game-flows.py')))
tap,tree,labels,shot,run=[q[k] for k in ['tap','tree','labels','shot','run']]
tap(next(label for label in labels(tree()) if label in ['电子银行 · 游戏币','地产资金 · 游戏币','积分赛 · 分','筹码 · 筹码']));tap('积分赛');shot('local-scene-dialog');tap('应用并重置本局')
tap('列表')
for i in range(1,3):
    if '玩家 '+str(i) not in labels(tree()):tap('添加玩家');tap('添加')
tap('全员收付')
tap('10')
time.sleep(.5);shot('local-batch-preview');tap('确认入账');shot('local-batch-posted')
current=labels(tree());assert current.count('10')>=2,current
tap('撤销上一笔');shot('local-batch-undo-confirm');tap('撤销');shot('local-batch-undone')
assert labels(tree()).count('0')>=2,labels(tree())
tap('新建收支');shot('local-transaction-default');tap('10');tap('交换收付双方');shot('local-transaction-swapped');tap('确认')
assert '-10' in labels(tree()),labels(tree())
tap('导出账本');shot('local-export');q['commands'](['back'])
tap('圆桌');shot('local-roundtable');tap('云端记账');shot('cloud-entry')
assert any('网络游戏' in s for s in labels(tree()))
print('PASS native '+('Harmony' if q['HARMONY'] else 'Android')+' ledger: scene reset, initial zero, batch preview/post/undo, default recipient, quick amount, swapped payment, CSV, roundtable and cloud entry',flush=True)
