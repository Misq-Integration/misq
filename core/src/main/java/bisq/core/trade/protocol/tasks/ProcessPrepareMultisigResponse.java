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

package bisq.core.trade.protocol.tasks;

import static bisq.core.util.Validator.checkTradeId;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

import bisq.common.taskrunner.TaskRunner;
import bisq.core.trade.MakerTrade;
import bisq.core.trade.TakerTrade;
import bisq.core.trade.Trade;
import bisq.core.trade.messages.PrepareMultisigResponse;
import lombok.extern.slf4j.Slf4j;
import monero.wallet.MoneroWalletJni;
import monero.wallet.model.MoneroMultisigInitResult;

@Slf4j
public class ProcessPrepareMultisigResponse extends TradeTask {
    @SuppressWarnings({"unused"})
    public ProcessPrepareMultisigResponse(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            log.debug("current trade state " + trade.getState());
            PrepareMultisigResponse response = (PrepareMultisigResponse) processModel.getTradeMessage();
            checkNotNull(response);
            checkTradeId(processModel.getOfferId(), response);
            
            // set trader's prepared multisig hex
            if (trade.getMakerNodeAddress().equals(response.getSenderNodeAddress())) {
              trade.setMakerPreparedMultisigHex(response.getPreparedMultisigHex());
            } else if (trade.getArbitratorNodeAddress().equals(response.getSenderNodeAddress())) {
              trade.setArbitratorPreparedMultisigHex(response.getPreparedMultisigHex());
            }  else {
              throw new RuntimeException("PrepareMultisigRequest not from taker or maker.  This should never happen.");
            }
            
            // make multisig  // TODO (woodser): lock to prevent concurrent access
            if (trade.getTakerPreparedMultisigHex() != null && trade.getMakerPreparedMultisigHex() != null && trade.getArbitratorPreparedMultisigHex() != null) {
              MoneroWalletJni wallet = trade.getXmrWalletService().getOrCreateMultisigWallet(trade.getId());
              MoneroMultisigInitResult result;
              if (trade instanceof TakerTrade) {
                result = wallet.makeMultisig(Arrays.asList(trade.getMakerPreparedMultisigHex(), trade.getArbitratorPreparedMultisigHex()), 2, "supersecretpassword123");  // TODO (woodser): password to config
                //trade.setTakerMadeMultisigHex(result.getMultisigHex());
              } else if (trade instanceof MakerTrade) {
                result = wallet.makeMultisig(Arrays.asList(trade.getTakerPreparedMultisigHex(), trade.getArbitratorPreparedMultisigHex()), 2, "supersecretpassword123");  // TODO (woodser): password to config
                //trade.setMakerMadeMultisigHex(result.getMultisigHex());
              } else throw new RuntimeException("Maker or taker trade expected.");
            }
            
            // if response if from arbitrator
            

            
            throw new RuntimeException("ProcessPrepareMultisigResponse not yet implemented");

//            final TradingPeer tradingPeer = processModel.getTradingPeer();
//            tradingPeer.setPaymentAccountPayload(checkNotNull(prepareMultisigRequest.getPaymentAccountPayload()));
//            
//            tradingPeer.setPayoutAddressString(nonEmptyStringOf(prepareMultisigRequest.getPayoutAddressString()));
//            tradingPeer.setPubKeyRing(checkNotNull(prepareMultisigRequest.getPubKeyRing()));
//
//            tradingPeer.setAccountId(nonEmptyStringOf(prepareMultisigRequest.getAccountId()));
//            trade.setTakerFeeTxId(nonEmptyStringOf(prepareMultisigRequest.getTradeFeeTxId()));
//
//            // Taker has to sign offerId (he cannot manipulate that - so we avoid to have a challenge protocol for passing the nonce we want to get signed)
//            tradingPeer.setAccountAgeWitnessNonce(trade.getId().getBytes(Charsets.UTF_8));
//            tradingPeer.setAccountAgeWitnessSignature(prepareMultisigRequest.getAccountAgeWitnessSignatureOfOfferId());
//            tradingPeer.setCurrentDate(prepareMultisigRequest.getCurrentDate());
//
//            User user = checkNotNull(processModel.getUser(), "User must not be null");
//
//            NodeAddress mediatorNodeAddress = checkNotNull(prepareMultisigRequest.getArbitratorNodeAddress(), // TODO (woodser): rename to getMediatorNodeAddress() in model?
//                    "payDepositRequest.getMediatorNodeAddress() must not be null");
//            trade.setMediatorNodeAddress(mediatorNodeAddress);
//            Mediator mediator = checkNotNull(user.getAcceptedMediatorByAddress(mediatorNodeAddress),
//                    "user.getAcceptedMediatorByAddress(mediatorNodeAddress) must not be null");
//            trade.setMediatorPubKeyRing(checkNotNull(mediator.getPubKeyRing(),
//                    "mediator.getPubKeyRing() must not be null"));
//
//            Offer offer = checkNotNull(trade.getOffer(), "Offer must not be null");
//            try {
//                long takersTradePrice = prepareMultisigRequest.getTradePrice();
//                offer.checkTradePriceTolerance(takersTradePrice);
//                trade.setTradePrice(takersTradePrice);
//            } catch (TradePriceOutOfToleranceException e) {
//                failed(e.getMessage());
//            } catch (Throwable e2) {
//                failed(e2);
//            }
//
//            checkArgument(prepareMultisigRequest.getTradeAmount() > 0);
//            trade.setTradeAmount(Coin.valueOf(prepareMultisigRequest.getTradeAmount()));
//            
//            // TODO (woodser): TradingPeer can be taker, maker, or arbitrator?  can have preparedMultisigHex?  trade.getArbitrator(), trade.getTradingPeer() ?
//            if (!prepareMultisigRequest.getMakerNodeAddress().equals(processModel.getMyNodeAddress())) throw new RuntimeException("Request's maker node address != my node address.  This should never happen.");
//            trade.setTradingPeerNodeAddress(processModel.getTempTradingPeerNodeAddress());
//            trade.setTakerPreparedMultisigHex(checkNotNull(prepareMultisigRequest.getPreparedMultisigHex()));
//            trade.setTakerNodeAddress(prepareMultisigRequest.getTakerNodeAddress());
//            trade.setMakerNodeAddress(prepareMultisigRequest.getMakerNodeAddress());
//            trade.setArbitratorNodeAddress(prepareMultisigRequest.getArbitratorNodeAddress());
//            trade.setArbitratorPubKeyRing(mediator.getPubKeyRing());
//
//            trade.persist();
//
//            complete();
        } catch (Throwable t) {
            failed(t);
        }
    }
}
