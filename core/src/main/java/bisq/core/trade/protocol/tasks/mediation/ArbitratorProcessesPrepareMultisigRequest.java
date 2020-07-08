/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.trade.protocol.tasks.mediation;

import static com.google.common.base.Preconditions.checkNotNull;

import bisq.common.taskrunner.TaskRunner;
import bisq.core.support.dispute.mediation.MediationResultState;
import bisq.core.trade.Trade;
import bisq.core.trade.messages.PrepareMultisigRequest;
import bisq.core.trade.protocol.tasks.TradeTask;
import bisq.core.util.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArbitratorProcessesPrepareMultisigRequest extends TradeTask {
  
    @SuppressWarnings({"unused"})
    public ArbitratorProcessesPrepareMultisigRequest(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            System.out.println("ArbitratorProcessesPrepareMultisigRequest.run()");
            log.debug("current trade state " + trade.getState());
            PrepareMultisigRequest message = (PrepareMultisigRequest) processModel.getTradeMessage();
            Validator.checkTradeId(processModel.getOfferId(), message);
            checkNotNull(message);
            
            if (true) throw new RuntimeException("ARBITRATOR READY TO PROCESS PREPARE MULTISIG REQUEST");
            //processModel.getTradingPeer().setMediatedPayoutTxSignature(checkNotNull(message.getTxSignature()));

            // update to the latest peer address of our peer if the message is correct
            trade.setTradingPeerNodeAddress(processModel.getTempTradingPeerNodeAddress());
            processModel.removeMailboxMessageAfterProcessing(trade);

            trade.setMediationResultState(MediationResultState.RECEIVED_SIG_MSG);

            complete();
        } catch (Throwable t) {
            failed(t);
        }
    }
}
