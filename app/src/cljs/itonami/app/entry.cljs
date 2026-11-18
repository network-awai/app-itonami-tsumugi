(ns itonami.app.entry
  "Tsumugi on the device (AWAI-3502). Calls itonami.ie-flow.metrics and
  tsumugi.methods.fission-gate unchanged. Not included: ingest, publish (IPFS),
  the power-structure analyzers that read the repo's data files, and the
  gate-adapter (file I/O). Examples are fictional."
  (:require [itonami.app.run :as run]
            [itonami.ie-flow.metrics :as m]
            [tsumugi.methods.fission-gate :as fission]))

(run/register!
 {:name "flow-state" :label "流れの状態をまとめる"
  :help "流れの記録（送り手・受け手・種類・量・費用・価値・リスク）を辺にまとめ、正味の利得・秩序化の度合い・エージェントの効率を計算します。"
  :example "{:events [{:source :member-a :target :commons :type :contribution :volume 3 :value 1200 :cost 200 :risk 0}
          {:source :member-b :target :commons :type :contribution :volume 1 :value 400 :cost 50 :risk 10}
          {:source :agent-x :target :commons :type :curation :volume 2 :value 300 :cost 120 :risk 0 :agent? true}]}"
  :run (fn [{:keys [events]}] (let [s (m/flow-state events)] (assoc s :summary (m/summary-line s))))})

(run/register!
 {:name "order-index" :label "秩序化の度合いを測る"
  :help "前と後の分布（量のならび）から、1 − H(後)/H(前) を計算します。散らばっていたものが少数にまとまるほど 1 に近づきます。"
  :example "{:before [5 5 5 5] :after [12 4 2 2]}"
  :run (fn [{:keys [before after]}]
         {:order-index (m/order-index (m/normalize before) (m/normalize after))
          :entropy-before (m/entropy (m/normalize before))
          :entropy-after (m/entropy (m/normalize after))})})

(run/register!
 {:name "fission-proposals" :label "審査への提案を作る"
  :help "準備ができた（:fission-ready）候補だけを、評議会の審査に回す提案にします。提案は決定ではなく、DID の発行もしません。"
  :example "{:latent [{\":latent/organism\" \"org-example-1\" \":latent/frontier\" \":fission-ready\" \":latent/existence\" 0.82 \":latent/evidence-count\" 5}
          {\":latent/organism\" \"org-example-2\" \":latent/frontier\" \":growing\" \":latent/existence\" 0.4 \":latent/evidence-count\" 2}]}"
  :run (fn [{:keys [latent]}] (fission/emit-proposals (fission/select-fission-ready latent)))})
